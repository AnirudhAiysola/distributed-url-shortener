package com.kyo.url_shortener.url;

import com.kyo.url_shortener.shortcode.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    static final int MAX_GENERATION_ATTEMPTS = 3;

    private final ShortUrlRepository repository;
    private final ShortCodeGenerator generator;
    private final Clock clock;

    // Deliberately NOT @Transactional: see concepts
    public ShortUrl create(CreateShortUrlCommand command) {
        Instant now = clock.instant();

        if(command.expiresAt() != null && !command.expiresAt().isAfter(now)){
            throw new InvalidExpiryException(command.expiresAt());
        }

        if(command.alias() != null){
            return createWithAlias(command, now);
        }
        return createWithGeneratedCode(command,now);
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
    public ShortUrl resolve(String code) {

        ShortUrl url = repository.findById(code)
                .orElseThrow(() -> new ShortUrlNotFoundException(code));

        if (url.isExpired(clock.instant())) {
            throw new ShortUrlExpiredException(code);
        }

        return url;
    }
}