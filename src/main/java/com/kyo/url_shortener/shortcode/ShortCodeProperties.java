package com.kyo.url_shortener.shortcode;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shortener.code")
public record ShortCodeProperties(long multiplier) {
}