package com.kyo.url_shortener.shortcode;

public class Base62 {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int CODE_LENGTH = 7;

    private static final long MAX_VALUE = 3_521_614_606_208L;

    private Base62(){}

     static String encode(long value){
        if(value < 0 || value >= MAX_VALUE){
            throw  new IllegalArgumentException("Invalid value for encoding");
        }
        char[] result = new char[CODE_LENGTH];

        for(int i=CODE_LENGTH-1;i>=0;i--){
            result[i] = ALPHABET.charAt((int) (value % 62));
            value = value/62;
        }
        return new String(result);
    }
}
