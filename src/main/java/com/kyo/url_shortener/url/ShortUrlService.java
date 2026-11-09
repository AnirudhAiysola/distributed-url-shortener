package com.kyo.url_shortener.url;

import com.kyo.url_shortener.common.SingleFlight;
import com.kyo.url_shortener.shortcode.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    static final int MAX_GENERATION_ATTEMPTS = 3;
    static final Duration MAX_CACHE_TTL = Duration.ofHours(24);

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator generator;
    private final Clock clock;
    private final ShortUrlCache cache;
    private final SingleFlight<String> singleFlight = new SingleFlight<>();

    // Deliberately NOT @Transactional: see concepts
    public ShortUrl create(CreateShortUrlCommand command) {
        Instant now = clock.instant();

        if(command.expiresAt() != null && !command.expiresAt().isAfter(now)){
            throw new InvalidExpiryException(command.expiresAt());
        }
        ShortUrl saved = null;
        if(command.alias() != null){
            saved = createWithAlias(command, now);
        }else{
            saved = createWithGeneratedCode(command,now);
        }
        cache.evict(saved.getCode());
        return saved;
    }

    private ShortUrl createWithAlias(CreateShortUrlCommand command, Instant now) {
        ShortUrl url = new ShortUrl(command.alias(), command.longUrl(), command.expiresAt(), now);
        try{
            ShortUrl saved = repository.saveAndFlush(url);
            return saved;
        }catch(DataIntegrityViolationException ex){
            throw new AliasAlreadyTakenException(command.alias());
        }
    }

    private ShortUrl createWithGeneratedCode(CreateShortUrlCommand command, Instant now) {

        for(int i=1;i<=MAX_GENERATION_ATTEMPTS;i++){
            String code = generator.generate();
            ShortUrl url = new ShortUrl(code, command.longUrl(), command.expiresAt(), now);
            try{
                ShortUrl saved = repository.saveAndFlush(url);
                return saved;
            }catch(DataIntegrityViolationException ex){
            }
        }
        throw new IllegalStateException("Could not generate a new code");
    }
    public String resolve(String code) {
        Optional<String> cached = cache.get(code);
        if (cached.isPresent()) {
            if (cached.get().equals(ShortUrlCache.MISSING)) {
                throw new ShortUrlNotFoundException(code);
            }
            return cached.get();
        }
        return singleFlight.execute(code, () -> loadFromDatabase(code));

    }

    private Duration cacheTtl(ShortUrl url, Instant now) {
        if(url.getExpiresAt() == null) return MAX_CACHE_TTL;
        Duration untilExpiry = Duration.between(now, url.getExpiresAt());

        return untilExpiry.compareTo(MAX_CACHE_TTL) < 0 ? untilExpiry : MAX_CACHE_TTL;
    }
    private String loadFromDatabase(String code) {
        Instant now = clock.instant();

        Optional<ShortUrl> found = repository.findById(code);
        if (found.isEmpty()) {
            cache.putMissing(code);
            throw new ShortUrlNotFoundException(code);
        }
        ShortUrl url = found.get();

        if (url.isExpired(now)) {
            throw new ShortUrlExpiredException(code);
        }

        cache.put(code, url.getLongUrl(), cacheTtl(url, now));
        return url.getLongUrl();
    }
}