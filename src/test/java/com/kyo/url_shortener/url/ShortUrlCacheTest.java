package com.kyo.url_shortener.url;

import com.kyo.url_shortener.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ShortUrlCacheTest {

    @Autowired
    ShortUrlCache cache;

    @Autowired
    StringRedisTemplate redis;
    @Test
    void putThenGetReturnsTheValue() {
        cache.put("cache01", "https://example.com", Duration.ofHours(1));

        assertThat(cache.get("cache01")).contains("https://example.com");
    }

    @Test
    void missingKeyReturnsEmpty() {
        assertThat(cache.get("missing01")).isEmpty();
    }

    @Test
    void putSetsTheTtl() {
        cache.put("cache02", "https://example.com", Duration.ofHours(1));

        Long ttlSeconds = redis.getExpire("url:cache02");

        assertThat(ttlSeconds).isBetween(3500L, 3600L);
    }
    @Test
    void putMissingStoresTheMarkerWithShortTtl() {
        cache.putMissing("missing02");

        assertThat(cache.get("missing02")).contains(ShortUrlCache.MISSING);
        assertThat(redis.getExpire("url:missing02")).isBetween(55L, 60L);
    }

    @Test
    void evictRemovesTheEntry() {
        cache.put("evict01", "https://example.com", Duration.ofHours(1));

        cache.evict("evict01");

        assertThat(cache.get("evict01")).isEmpty();
    }
}