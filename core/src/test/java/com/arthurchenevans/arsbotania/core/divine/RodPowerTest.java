package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/** The Divine Rod holds every Botania rod's power; one is selected at a time. */
class RodPowerTest {

    @Test
    void allThirteenBotaniaRods() {
        assertEquals(13, RodPower.values().length);
        HashSet<String> ids = new HashSet<>();
        for (RodPower p : RodPower.values()) ids.add(p.botaniaItem());
        assertEquals(13, ids.size());
        assertTrue(ids.contains("botania:rod_of_the_lands"));
        assertTrue(ids.contains("botania:rod_of_the_unstable_reservior"), "Botania's own spelling");
    }

    @Test
    void startsOnTheLandsAndCyclesThroughEveryPowerBackToTheStart() {
        RodPower p = RodPower.DEFAULT;
        assertEquals(RodPower.LANDS, p);
        HashSet<RodPower> seen = new HashSet<>();
        for (int i = 0; i < RodPower.values().length; i++) {
            seen.add(p);
            p = p.next();
        }
        assertEquals(RodPower.values().length, seen.size());
        assertEquals(RodPower.DEFAULT, p);
    }

    @Test
    void storedIndexRoundTripsAndBadValuesFallBack() {
        for (RodPower p : RodPower.values()) assertEquals(p, RodPower.fromIndex(p.ordinal()));
        assertEquals(RodPower.DEFAULT, RodPower.fromIndex(99));
    }
}
