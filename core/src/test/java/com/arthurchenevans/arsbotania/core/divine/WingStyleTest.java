package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WingStyleTest {

    @Test
    void robesStartWithDivineWings() {
        assertEquals(WingStyle.DIVINE, WingStyle.DEFAULT);
    }

    @Test
    void botaniaStylesKeepTheTiarasVariantNumbers() {
        assertEquals(0, WingStyle.NONE.botaniaVariant());
        assertEquals(1, WingStyle.FLUGEL.botaniaVariant());
        assertEquals(8, WingStyle.MEGA_ULTRA_CHICKEN.botaniaVariant());
        assertEquals(9, WingStyle.THE_ONE.botaniaVariant());
        assertTrue(WingStyle.FLUGEL.isBotaniaWings());
        assertFalse(WingStyle.DIVINE.isBotaniaWings());
        assertFalse(WingStyle.NONE.isBotaniaWings());
    }

    @Test
    void sneakUseCyclesDivineThenNoneThenBotaniasEightStylesAndBack() {
        List<WingStyle> seen = new ArrayList<>();
        WingStyle style = WingStyle.DEFAULT;
        for (int i = 0; i < 11; i++) {
            seen.add(style);
            style = style.next();
        }

        assertEquals(List.of(WingStyle.DIVINE, WingStyle.NONE, WingStyle.FLUGEL, WingStyle.ONE_WINGED_ANGEL,
                WingStyle.ICE_FAIRY, WingStyle.PHOENIX, WingStyle.BLACK_SNOW_PRINCESS, WingStyle.LORD_OF_THE_PIT,
                WingStyle.SYLPH, WingStyle.MEGA_ULTRA_CHICKEN, WingStyle.DIVINE), seen);
    }

    @Test
    void theOneStaysBotaniasSecretAndIsNotInTheCycle() {
        WingStyle style = WingStyle.DEFAULT;
        for (int i = 0; i < 20; i++) {
            style = style.next();
            assertNotEquals(WingStyle.THE_ONE, style);
        }
        assertEquals(WingStyle.DIVINE, WingStyle.THE_ONE.next());
    }

    @Test
    void storedIdsRoundTripAndUnknownIdsFallBackToDivine() {
        for (WingStyle style : WingStyle.values()) {
            assertEquals(style, WingStyle.fromId(style.id()));
        }
        assertEquals(WingStyle.DIVINE, WingStyle.fromId(99));
        assertEquals(WingStyle.DIVINE, WingStyle.fromId(-1));
    }
}
