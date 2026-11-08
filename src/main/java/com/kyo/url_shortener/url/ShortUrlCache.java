package com.kyo.url_shortener.url;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShortUrlCache {

    private static final String KEY_PREFIX = "url:";
    static final String MISSING = "__missing__";
    static final Duration NEGATIVE_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;

    // Initialized here, so Lombok leaves it out of the constructor
    private final CircuitBreaker breaker = CircuitBreaker.of("redis", CircuitBreakerConfig.custom()
            .slidingWindowSize(20)                        // look at the last 20 calls
            .minimumNumberOfCalls(10)                     // need at least 10 before judging
            .failureRateThreshold(50)                     // open if ≥50% failed
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build());

    public Optional<String> get(String code) {
        try {
            return breaker.executeSupplier(() -> Optional.ofNullable(redis.opsForValue().get(key(code))));
        } catch (CallNotPermittedException ex) {
            return Optional.empty();
        } catch (DataAccessException ex) {
            log.warn("Redis get failed for {}: {}", code, ex.getMessage());
            return Optional.empty();
        }
    }

    public void put(String code, String longUrl, Duration ttl) {
        try{
            breaker.executeRunnable(() -> redis.opsForValue().set(key(code), longUrl, ttl));
        }catch(CallNotPermittedException ex){

        }catch(DataAccessException ex){

        }
    }

    public void putMissing(String code) {
        put(code, MISSING, NEGATIVE_TTL);   // reuses put's protection
    }

    public void evict(String code) {
        try{
            breaker.executeRunnable(() -> redis.delete(key(code)));
        }catch(CallNotPermittedException ex){

        }catch(DataAccessException ex){

        }
    }

    private String key(String code) {
        return KEY_PREFIX + code;
    }
}