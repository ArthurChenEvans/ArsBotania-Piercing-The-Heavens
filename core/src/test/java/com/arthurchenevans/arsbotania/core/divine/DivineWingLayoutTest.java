package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Divine wings: three pairs like an archangel, all bigger than Botania's Flügel wings. */
class DivineWingLayoutTest {

    private static final float BOTANIA_FLUGEL_SCALE = 1.5f;

    @Test
    void archangelsHaveThreePairs() {
        assertEquals(3, DivineWingLayout.PAIRS.size());
    }

    @Test
    void everyPairIsBiggerThanBotaniasWings() {
        DivineWingLayout.PAIRS.forEach(p -> assertTrue(p.scale() > BOTANIA_FLUGEL_SCALE, p.toString()));
    }

    @Test
    void pairsShrinkFromTopToBottom() {
        List<DivineWingLayout.Pair> pairs = DivineWingLayout.PAIRS;
        for (int i = 1; i < pairs.size(); i++) {
            assertTrue(pairs.get(i).scale() < pairs.get(i - 1).scale());
            assertTrue(pairs.get(i).height() < pairs.get(i - 1).height(), "lower pairs sit lower on the back");
        }
    }

    @Test
    void pairsFlapOutOfStep() {
        assertNotEquals(DivineWingLayout.flap(DivineWingLayout.PAIRS.get(0), 10f, true),
                DivineWingLayout.flap(DivineWingLayout.PAIRS.get(1), 10f, true));
    }

    @Test
    void flyingFlapsHarderThanStanding() {
        DivineWingLayout.Pair top = DivineWingLayout.PAIRS.getFirst();
        float standingRange = 0, flyingRange = 0;
        for (int t = 0; t < 100; t++) {
            standingRange = Math.max(standingRange, Math.abs(DivineWingLayout.flap(top, t, false) - DivineWingLayout.flap(top, 0, false)));
            flyingRange = Math.max(flyingRange, Math.abs(DivineWingLayout.flap(top, t, true) - DivineWingLayout.flap(top, 0, true)));
        }
        assertTrue(flyingRange > standingRange);
    }

    @Test
    void haloHueCyclesAndEachCornerIsADifferentColour() {
        float a = DivineWingLayout.haloHue(0f, 0), b = DivineWingLayout.haloHue(0f, 1);
        assertNotEquals(a, b);
        assertEquals(DivineWingLayout.haloHue(0f, 0), DivineWingLayout.haloHue(DivineWingLayout.HALO_CYCLE_TICKS, 0), 1e-5);
        for (int corner = 0; corner < 4; corner++) {
            float hue = DivineWingLayout.haloHue(37f, corner);
            assertTrue(hue >= 0 && hue < 1, "hue in [0,1): " + hue);
        }
    }
}
