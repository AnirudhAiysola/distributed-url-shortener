package com.kyo.url_shortener.url;

import com.kyo.url_shortener.shortcode.ShortCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortUrlServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");
    private static final String LONG_URL = "https://example.com/some/long/path";

    private final ShortUrlRepository repository = mock(ShortUrlRepository.class);
    private final ShortCodeGenerator generator = mock(ShortCodeGenerator.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final ShortUrlService service = new ShortUrlService(repository, generator, clock);

    // Normal path: no alias, so the service generates a code and saves it.
    @Test
    void createsWithGeneratedCode() {
        when(generator.generate()).thenReturn("abc1234");
        when(repository.saveAndFlush(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(new CreateShortUrlCommand(LONG_URL, null, null));

        assertThat(result.getCode()).isEqualTo("abc1234");
        assertThat(result.getLongUrl()).isEqualTo(LONG_URL);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);  // proves the injected Clock is used
    }

    // Alias given: it becomes the code, and the generator is skipped.
    @Test
    void createsWithCustomAliasWithoutGenerating() {
        when(repository.saveAndFlush(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(new CreateShortUrlCommand(LONG_URL, "sai-resume", null));

        assertThat(result.getCode()).isEqualTo("sai-resume");
        verify(generator, never()).generate();
    }

    // Fake Postgres rejecting a duplicate → service must say "alias taken".
    @Test
    void takenAliasThrowsAliasAlreadyTaken() {
        when(repository.saveAndFlush(any(ShortUrl.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.create(new CreateShortUrlCommand(LONG_URL, "sai-resume", null)))
                .isInstanceOf(AliasAlreadyTakenException.class);
    }

    // First generated code collides, second succeeds → service retries and returns the second.
    @Test
    void retriesWhenGeneratedCodeCollides() {
        when(generator.generate()).thenReturn("aaaaaaa", "bbbbbbb");
        when(repository.saveAndFlush(any(ShortUrl.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"))  // 1st save fails
                .thenAnswer(inv -> inv.getArgument(0));                       // 2nd save succeeds

        ShortUrl result = service.create(new CreateShortUrlCommand(LONG_URL, null, null));

        assertThat(result.getCode()).isEqualTo("bbbbbbb");
    }

    // Every save collides → service gives up after exactly MAX_GENERATION_ATTEMPTS tries.
    @Test
    void givesUpAfterMaxAttempts() {
        when(generator.generate()).thenReturn("aaaaaaa");
        when(repository.saveAndFlush(any(ShortUrl.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));  // every time

        assertThatThrownBy(() -> service.create(new CreateShortUrlCommand(LONG_URL, null, null)))
                .isInstanceOf(IllegalStateException.class);
        verify(generator, times(ShortUrlService.MAX_GENERATION_ATTEMPTS)).generate();
    }

    // Expiry in the past is rejected before anything is saved.
    @Test
    void rejectsExpiryInThePast() {
        assertThatThrownBy(() -> service.create(new CreateShortUrlCommand(LONG_URL, null, NOW.minusSeconds(60))))
                .isInstanceOf(InvalidExpiryException.class);
    }

    // Boundary: expiring exactly now is also rejected.
    @Test
    void rejectsExpiryEqualToNow() {
        assertThatThrownBy(() -> service.create(new CreateShortUrlCommand(LONG_URL, null, NOW)))
                .isInstanceOf(InvalidExpiryException.class);
    }
}