package com.kyo.url_shortener.shortcode;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ShortCodeProperties.class)
public class ShortCodeConfig {

    @Bean
    Scrambler scrambler(ShortCodeProperties properties) {
        Scrambler scrambler = new Scrambler(properties.multiplier());
        return scrambler;
    }
}