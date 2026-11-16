package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The time component: start at sunset, keep mana flowing all night, finish at sunrise. */
class TimeRitualTest {
    private static final int GAP = 100;

    @Test
    void canOnlyStartAtSunset() {
        assertTrue(TimeRitual.canStart(12_000));
        assertTrue(TimeRitual.canStart(12_999));
        assertFalse(TimeRitual.canStart(11_999), "still day");
        assertFalse(TimeRitual.canStart(13_000), "already night");
        assertTrue(TimeRitual.canStart(24_000 * 5 + 12_500), "any day's sunset");
    }

    @Test
    void sunriseIsTheMorningAfterTheStart() {
        TimeRitual r = TimeRitual.begin(1_000, 12_500);
        assertEquals(1_000 + (23_000 - 12_500), r.sunriseGameTime());
    }

    @Test
    void steadyManaAllNightReachesReady() {
        TimeRitual r = TimeRitual.begin(0, 12_000);
        TimeRitual.State state = TimeRitual.State.RUNNING;
        for (long t = 1; t <= 11_000; t++) state = r.update(t, true, true, GAP);
        assertEquals(TimeRitual.State.READY, state);
    }

    @Test
    void notReadyBeforeSunrise() {
        TimeRitual r = TimeRitual.begin(0, 12_000);
        for (long t = 1; t <= 5_000; t++) r.update(t, true, true, GAP);
        assertEquals(TimeRitual.State.RUNNING, r.update(5_001, true, true, GAP));
    }

    @Test
    void aGapInTheManaFailsIt() {
        TimeRitual r = TimeRitual.begin(0, 12_000);
        r.update(1, true, true, GAP);
        assertEquals(TimeRitual.State.RUNNING, r.update(1 + GAP, false, true, GAP));
        assertEquals(TimeRitual.State.FAILED, r.update(2 + GAP, false, true, GAP));
    }

    @Test
    void unloadingTheChunkFailsIt() {
        TimeRitual r = TimeRitual.begin(0, 12_000);
        assertEquals(TimeRitual.State.FAILED, r.update(5, true, false, GAP));
    }

    @Test
    void onceFailedItStaysFailed() {
        TimeRitual r = TimeRitual.begin(0, 12_000);
        r.update(5, true, false, GAP);
        assertEquals(TimeRitual.State.FAILED, r.update(6, true, true, GAP));
    }

    @Test
    void cannotBeginOutsideSunset() {
        assertNull(TimeRitual.begin(0, 6_000));
    }

    @Test
    void manaIsHeldBelowFullUntilSunrise() {
        assertEquals(999, TimeRitual.manaCap(TimeRitual.State.RUNNING, 1_000));
        assertEquals(1_000, TimeRitual.manaCap(TimeRitual.State.READY, 1_000));
    }
}
