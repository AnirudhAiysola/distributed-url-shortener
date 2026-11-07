package com.kyo.url_shortener.url;

public class AliasAlreadyTakenException extends RuntimeException {

    public AliasAlreadyTakenException(String alias) {
        super("Alias already taken: " + alias);
    }
}