package com.kyo.url_shortener.url;

import java.time.Instant;

public record ShortUrlResponse(
        String code,
        String shortUrl,
        String longUrl,
        Instant expiresAt,
        Instant createdAt
) {
    public static ShortUrlResponse from(ShortUrl url, String baseUrl) {

        return new ShortUrlResponse(url.getCode(), baseUrl + "/" + url.getCode(), url.getLongUrl(), url.getExpiresAt(), url.getCreatedAt());
    }
}