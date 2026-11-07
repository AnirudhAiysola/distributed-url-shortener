package com.kyo.url_shortener.url;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String code) {
        super("No short URL found for code: " + code);
    }
}