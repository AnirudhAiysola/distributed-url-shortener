package com.kyo.url_shortener.shortcode;


import java.math.BigInteger;

public final class Scrambler {

    private final BigInteger multiplier;
    private static final BigInteger MODULUS = BigInteger.valueOf(Base62.MAX_VALUE);


    public Scrambler(long multiplier){
        if(multiplier % 2 == 0 || multiplier % 31 == 0) {
            throw new IllegalArgumentException("Multiplier must be odd and not divisible by 31");
        }
        this.multiplier = BigInteger.valueOf(multiplier);
    }

    public long scramble(long id){
        if(id < 0 || id >= Base62.MAX_VALUE){
            throw new IllegalArgumentException("Id is not within valid range for scrambling: " + id);
        }
        return BigInteger.valueOf(id).multiply(multiplier).mod(MODULUS).longValueExact();
    }
}
