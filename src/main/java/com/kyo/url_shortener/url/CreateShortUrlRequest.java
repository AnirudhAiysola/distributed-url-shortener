package com.kyo.url_shortener.url;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateShortUrlRequest(

        @NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://\\S+$")
        String longUrl,


        @Size(min = 3, max = 32) @Pattern(regexp = "^[A-Za-z0-9_-]+$")
        String alias,

        // optional; the service checks it's in the future
        Instant expiresAt
) {
    public CreateShortUrlCommand toCommand() {
        return new CreateShortUrlCommand(longUrl, alias, expiresAt);
    }
}