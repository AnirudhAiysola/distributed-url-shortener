package com.kyo.url_shortener.shortcode;

import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

    private final IdBlockAllocator allocator;
    private final Scrambler scrambler;

    public ShortCodeGenerator(IdBlockAllocator allocator, Scrambler scrambler) {
        this.allocator = allocator;
        this.scrambler = scrambler;
    }

    public String generate() {
        long id = allocator.nextId();
        long obfuscatedId = scrambler.scramble(id);

        return Base62.encode(obfuscatedId);
    }
}