package com.kyo.url_shortener.shortcode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShortCodeGeneratorTest {

    private final IdBlockAllocator allocator = mock(IdBlockAllocator.class);
    private final Scrambler scrambler = new Scrambler(1_580_030_173L);
    private final ShortCodeGenerator generator = new ShortCodeGenerator(allocator, scrambler);

    @Test
    void generatesSevenCharacterCode() {
        when(allocator.nextId()).thenReturn(42L);
        String code = generator.generate();
        assertThat(code).hasSize(7);
    }

    @Test
    void generatesTheScrambledAndEncodedId() {
        when(allocator.nextId()).thenReturn(42L);
        String code = generator.generate();
        assertThat(code).isEqualTo(Base62.encode(scrambler.scramble(42)));
    }

    @Test
    void differentIdsGiveDifferentCodes() {
        when(allocator.nextId()).thenReturn(1L, 2L); // first call returns 1, second returns 2
        String code1 = generator.generate();
        String code2 = generator.generate();
        assertThat(code1).isNotEqualTo(code2);
    }
}