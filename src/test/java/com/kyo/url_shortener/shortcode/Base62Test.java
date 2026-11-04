package com.kyo.url_shortener.shortcode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class Base62Test {

    @Test
    void encodeZeroAsAllZeroes(){
        // Act
        String code = Base62.encode(0);
        // Assert
        assertThat(code).isEqualTo("0000000");
    }
    @Test
    void encodeLastSingleDigit(){
        // Act
        String code = Base62.encode(61);
        // Assert
        assertThat(code).isEqualTo("000000Z");
    }
    @Test
    void encodesFirstTwoDigitValue() {
        String code = Base62.encode(62);
        // Assert
        assertThat(code).isEqualTo("0000010");
    }
    @Test
    void encodesMaxValue(){
        String code = Base62.encode(3_521_614_606_207L);
        // Assert
        assertThat(code).isEqualTo("ZZZZZZZ");
    }
    @Test
    void rejectsNegativeValue(){
        assertThatThrownBy(() -> Base62.encode(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
