package com.kyo.url_shortener.url;

import com.kyo.url_shortener.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ShortUrlRepositoryTest {

    private static final Instant CREATED = Instant.parse("2026-11-05T10:00:00Z");

    @Autowired
    ShortUrlRepository repository;

    @Test
    void savesAndLoadsAUrl() {
        ShortUrl url = new ShortUrl("save001", "https://example.com",null,CREATED);
        ShortUrl saved = repository.save(url);
        ShortUrl loaded = repository.findById("save001").orElseThrow();

        assertThat(loaded.getLongUrl()).isEqualTo("https://example.com");
        assertThat(loaded.getCreatedAt()).isEqualTo(CREATED);
        assertThat(loaded.getExpiresAt()).isNull();
    }

    @Test
    void savingADuplicateCodeFails() {
        ShortUrl url1 = new ShortUrl("dup001", "https://example.com", null,CREATED);
        ShortUrl saved = repository.saveAndFlush(url1);

        ShortUrl url2 = new ShortUrl("dup001", "https://scam.com", null, CREATED.plusSeconds(1500));

        assertThatThrownBy(() -> repository.saveAndFlush(url2)).isInstanceOf(DataIntegrityViolationException.class);
    }
}