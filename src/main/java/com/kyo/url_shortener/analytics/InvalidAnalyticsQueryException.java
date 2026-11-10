package com.kyo.url_shortener.analytics;

public class InvalidAnalyticsQueryException extends RuntimeException {

    public InvalidAnalyticsQueryException(String message) {
        super(message);
    }
}