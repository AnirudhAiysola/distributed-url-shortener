package com.kyo.url_shortener.url;

public class ShortUrlExpiredException extends RuntimeException {

    public ShortUrlExpiredException(String code) {
        super("Short URL has expired: " + code);
    }
}