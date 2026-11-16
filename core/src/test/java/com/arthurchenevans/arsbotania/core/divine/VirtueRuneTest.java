package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtueRuneTest {

    @Test
    void eachSinHasItsVirtue() {
        assertEquals(VirtueRune.CHASTITY, VirtueRune.forSin("lust"));
        assertEquals(VirtueRune.TEMPERANCE, VirtueRune.forSin("gluttony"));
        assertEquals(VirtueRune.CHARITY, VirtueRune.forSin("greed"));
        assertEquals(VirtueRune.DILIGENCE, VirtueRune.forSin("sloth"));
        assertEquals(VirtueRune.PATIENCE, VirtueRune.forSin("wrath"));
        assertEquals(VirtueRune.KINDNESS, VirtueRune.forSin("envy"));
        assertEquals(VirtueRune.HUMILITY, VirtueRune.forSin("pride"));
    }

    @Test
    void sevenVirtuesWithBotaniaSinRuneIds() {
        assertEquals(7, VirtueRune.values().length);
        assertEquals("botania:rune_of_lust", VirtueRune.CHASTITY.sinRuneItem());
        assertEquals("rune_of_chastity", VirtueRune.CHASTITY.registryName());
    }
}
