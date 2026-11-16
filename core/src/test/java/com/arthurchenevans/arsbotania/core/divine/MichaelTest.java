package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Michael's rules: who earns virtue runes, and how the fight escalates. */
class MichaelTest {
    private static final int WINDOW = 600; // 30 seconds

    private static Set<String> allSins() {
        return Set.of("lust", "gluttony", "greed", "sloth", "wrath", "envy", "pride");
    }

    @Test
    void fightersWhoHitHimInTheLastThirtySecondsAndCarryEverySinRuneConvert() {
        assertTrue(Michael.earnsVirtues(1_000, 1_000 + WINDOW, WINDOW, allSins()));
    }

    @Test
    void anOlderHitDoesNotCount() {
        assertFalse(Michael.earnsVirtues(1_000, 1_001 + WINDOW, WINDOW, allSins()));
    }

    @Test
    void missingAnySinRuneMeansNoConversion() {
        Set<String> sixSins = new java.util.HashSet<>(allSins());
        sixSins.remove("pride");
        assertFalse(Michael.earnsVirtues(1_000, 1_100, WINDOW, sixSins));
    }

    @Test
    void conversionTurnsOneOfEachSinIntoItsVirtue() {
        Map<String, VirtueRune> conversion = Michael.conversion();
        assertEquals(7, conversion.size());
        assertEquals(VirtueRune.HUMILITY, conversion.get("pride"));
        assertEquals(VirtueRune.CHASTITY, conversion.get("lust"));
    }

    @Test
    void secondPhaseBelowHalfHealth() {
        assertEquals(Michael.Phase.ONE, Michael.phase(800, 800));
        assertEquals(Michael.Phase.ONE, Michael.phase(401, 800));
        assertEquals(Michael.Phase.TWO, Michael.phase(400, 800));
    }

    @Test
    void secondPhaseAddsLightningToTheMissiles() {
        assertEquals(EnumSet.of(Michael.Attack.TELEPORT, Michael.Attack.MISSILES), Michael.Phase.ONE.attacks());
        assertTrue(Michael.Phase.TWO.attacks().contains(Michael.Attack.LIGHTNING));
        assertTrue(Michael.Phase.TWO.attacks().containsAll(Michael.Phase.ONE.attacks()));
    }

    @Test
    void summonedFromAnActiveBeaconOnlyIfNoMichaelIsAlreadyThere() {
        assertTrue(Michael.canSummon(1, false));
        assertFalse(Michael.canSummon(0, false), "beacon not active");
        assertFalse(Michael.canSummon(4, true), "one Michael per beacon");
    }
}
