package com.kyo.url_shortener.url;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortUrlCacheFailureTest {

    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = mock(ValueOperations.class);
    private final ShortUrlCache cache = new ShortUrlCache(redis);

    @BeforeEach
    void wireOps() {
        when(redis.opsForValue()).thenReturn(ops);
    }

    @Test
    void getTreatsRedisFailureAsMiss() {
        when(ops.get(any())).thenThrow(new RedisConnectionFailureException("redis down"));

        assertThat(cache.get("abc1234")).isEmpty();
    }

    @Test
    void putSwallowsRedisFailure() {
        doThrow(new RedisConnectionFailureException("redis down"))
                .when(ops).set(any(), any(), any(Duration.class));

        assertThatCode(() -> cache.put("abc1234", "https://example.com", Duration.ofHours(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void breakerOpensAndStopsCallingRedis() {
        when(ops.get(any())).thenThrow(new RedisConnectionFailureException("redis down"));

        for (int i = 0; i < 50; i++) {
            cache.get("abc1234");
        }

        // After ~10 failures the breaker opens; the other ~40 calls never reach Redis
        verify(ops, atMost(10)).get(any());
    }
}