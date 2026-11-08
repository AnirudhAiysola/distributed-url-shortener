package com.kyo.url_shortener.url;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShortUrlCache {

    private static final String KEY_PREFIX = "url:";
    static final String MISSING = "__missing__";
    static final Duration NEGATIVE_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;

    public Optional<String> get(String code) {
       String redisKey = key(code);
        Optional<String> value = Optional.ofNullable(redis.opsForValue().get(redisKey));
        return value;
    }

    public void put(String code, String longUrl, Duration ttl) {
        String redisKey = key(code);
        redis.opsForValue().set(redisKey,longUrl,ttl);
    }
    private String key(String code) {
        return KEY_PREFIX + code;
    }


    public void putMissing(String code) {
        redis.opsForValue().set(key(code), MISSING,NEGATIVE_TTL);
    }

    public void evict(String code) {
        redis.delete(key(code));
    }
}