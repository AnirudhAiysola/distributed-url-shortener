package com.kyo.url_shortener.url;

import java.time.Instant;

// Input to the service. alias and expiresAt may be null.
public record CreateShortUrlCommand(String longUrl, String alias, Instant expiresAt) {
}