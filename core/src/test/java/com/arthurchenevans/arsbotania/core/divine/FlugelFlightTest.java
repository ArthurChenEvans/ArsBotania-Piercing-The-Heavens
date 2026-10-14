package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The Divine robes fly exactly like Botania's Flügel Tiara. */
class FlugelFlightTest {

    @Test
    void numbersAreTheTiaras() {
        assertEquals(1200, FlugelFlight.MAX_FLIGHT_TIME);
        assertEquals(35, FlugelFlight.COST);
        assertEquals(105, FlugelFlight.COST_OVERKILL);
        assertEquals(40, FlugelFlight.DASH_COOLDOWN);
    }

    @Test
    void costTriplesOnceTheFlightBarIsEmpty() {
        assertEquals(35, FlugelFlight.cost(1));
        assertEquals(105, FlugelFlight.cost(0));
    }

    @Test
    void takingOffNeedsATenthOfTheBarButStayingAloftNeedsAnyTime() {
        assertFalse(FlugelFlight.canFly(120, false, false, true), "exactly a tenth is not enough to take off");
        assertTrue(FlugelFlight.canFly(121, false, false, true));
        assertTrue(FlugelFlight.canFly(1, true, false, true));
        assertFalse(FlugelFlight.canFly(0, true, false, true));
    }

    @Test
    void eyeOfTheFlugelIgnoresTheBar() {
        assertTrue(FlugelFlight.canFly(0, true, true, true));
    }

    @Test
    void noManaNoFlight() {
        assertFalse(FlugelFlight.canFly(1200, false, true, false));
    }

    @Test
    void flyingDrainsOneTickAtATimeUnlessCreative() {
        assertEquals(99, FlugelFlight.nextRemaining(100, true, false, false, 0));
        assertEquals(100, FlugelFlight.nextRemaining(100, true, true, false, 0));
        assertEquals(0, FlugelFlight.nextRemaining(0, true, false, false, 0));
    }

    @Test
    void groundedRefillsEveryTwoTicksOrSixWhileGliding() {
        assertEquals(101, FlugelFlight.nextRemaining(100, false, false, false, 2));
        assertEquals(100, FlugelFlight.nextRemaining(100, false, false, false, 3));
        assertEquals(100, FlugelFlight.nextRemaining(100, false, false, true, 2));
        assertEquals(101, FlugelFlight.nextRemaining(100, false, false, true, 6));
        assertEquals(1200, FlugelFlight.nextRemaining(1200, false, false, false, 2));
    }

    @Test
    void glidingNeedsSneakingInTheAirWhileFallingFast() {
        assertTrue(FlugelFlight.shouldGlide(true, false, -0.8, false));
        assertFalse(FlugelFlight.shouldGlide(true, false, -0.5, false), "not falling fast enough to start");
        assertTrue(FlugelFlight.shouldGlide(true, false, -0.1, true), "keeps gliding once started");
        assertFalse(FlugelFlight.shouldGlide(false, false, -0.8, false));
        assertFalse(FlugelFlight.shouldGlide(true, true, -0.8, true));
    }
}
