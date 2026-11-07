package com.kyo.url_shortener.url;

import java.time.Instant;

public class InvalidExpiryException extends RuntimeException {

    public InvalidExpiryException(Instant expiresAt) {
        super("expiresAt must be in the future: " + expiresAt);
    }
}