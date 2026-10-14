package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** When Ars mana falls short for a grimoire spell, the gap is paid from Source jars, then Botania mana. */
class ManaBridgeTest {

    private final DivineArmorSettings s = DivineArmorSettings.defaults();

    @Test
    void enoughManaNeedsNoTopUp() {
        assertEquals(0, ManaBridge.deficit(50, 80, 500));
    }

    @Test
    void topsUpOnlyTheShortfall() {
        assertEquals(30, ManaBridge.deficit(50, 20, 500));
    }

    @Test
    void topUpBringsTheBarToExactlyTheSpellsCost() {
        // a spell within the bar can always be bridged without overfilling it
        assertEquals(500, 0 + ManaBridge.deficit(500, 0, 500));
        assertEquals(1, ManaBridge.deficit(50, 49.5, 500), "fractional mana rounds the top-up up");
    }

    @Test
    void spellsBiggerThanTheManaBarCannotBeBridged() {
        assertEquals(0, ManaBridge.deficit(600, 20, 500));
    }

    @Test
    void conversionRatesComeFromConfig() {
        assertEquals(30 * s.botaniaManaPerArsMana(), ManaBridge.botaniaCost(30, s));
        assertEquals(30 * s.sourcePerArsMana(), ManaBridge.sourceCost(30, s));
    }

    @Test
    void ratesAreTunableAndMustBePositive() {
        assertEquals(7, ManaBridge.botaniaCost(1, s.toBuilder().botaniaManaPerArsMana(7).build()));
        assertThrows(IllegalArgumentException.class, () -> s.toBuilder().botaniaManaPerArsMana(0).build());
        assertThrows(IllegalArgumentException.class, () -> s.toBuilder().jarRange(-1).build());
    }
}
