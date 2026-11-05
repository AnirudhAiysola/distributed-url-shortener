package com.kyo.url_shortener.url;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    @Test
    void linkWithoutExpiryNeverExpires() {
       ShortUrl url = new ShortUrl("abc1234", "https://example.com",null,NOW);
       boolean expired = url.isExpired(NOW);
       assertThat(expired).isFalse();
    }

    @Test
    void linkIsExpiredAfterExpiryTime() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com",NOW.minusSeconds(3600),NOW);
        boolean expired = url.isExpired(NOW);
        assertThat(expired).isTrue();
    }

    @Test
    void linkIsNotExpiredBeforeExpiryTime() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com",NOW.plusSeconds(3600),NOW);
        boolean expired = url.isExpired(NOW);
        assertThat(expired).isFalse();
    }

    @Test
    void linkIsExpiredExactlyAtExpiryTime() {
        ShortUrl url = new ShortUrl("abc1234", "https://example.com",NOW,NOW);
        boolean expired = url.isExpired(NOW);
        assertThat(expired).isTrue();
    }
}