package com.kyo.url_shortener.shortcode;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScramblerTest {

    private final Scrambler scrambler = new Scrambler(1_580_030_173L);

    @Test
    void rejectsEvenMultiplier() {
        assertThatThrownBy(() -> new Scrambler(2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMultiplierDivisibleBy31() {
        assertThatThrownBy(() -> new Scrambler(93)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resultsStayInRange() {
        // TODO: scramble a few ids (0, 1, 12345, Base62.MAX_VALUE - 1)
        // assert each result is >= 0 and < Base62.MAX_VALUE
        // hint: assertThat(x).isBetween(0L, Base62.MAX_VALUE - 1)
        long[] ids = {0,1,12345,Base62.MAX_VALUE - 1};
        for(long id : ids){
            long obfuscatedId = scrambler.scramble(id);
            assertThat(obfuscatedId).isBetween(0L, Base62.MAX_VALUE-1);
        }
    }

    @Test
    void noCollisionsForFirst100kIds() {
        Set<Long> seen = new HashSet<>();
        // TODO: loop id from 0 to 100_000 (inclusive), add scrambler.scramble(id) to seen
        // TODO: assert seen.size() is 100_001
        for(int i=0;i<=100000;i++){
            long id = scrambler.scramble((long)i);
            seen.add(id);
        }
        assertThat(seen.size()).isEqualTo(100_001);
    }

    @Test
    void consecutiveIdsAreNotConsecutive() {
        // TODO: scramble 1 and 2
        // assert the second result is NOT equal to first result + 1
        // hint: isNotEqualTo(...)
        long id1 = scrambler.scramble(1);
        long id2 = scrambler.scramble(2);

        assertThat(id2).isNotEqualTo(id1 + 1);
    }
}