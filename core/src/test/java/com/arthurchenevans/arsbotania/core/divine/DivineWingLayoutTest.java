package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Divine wings: one big pair, angled diagonally upward, rooted at the spine. */
class DivineWingLayoutTest {

    private static final float BOTANIA_FLUGEL_SCALE = 1.5f;
    private static final float BOTANIA_FLUGEL_TILT = -60f;
    private final DivineWingLayout.Pair wings = DivineWingLayout.WINGS;

    @Test
    void wingsAreClearlyBiggerThanBotanias() {
        assertTrue(wings.scale() >= BOTANIA_FLUGEL_SCALE * 1.5f, "scale " + wings.scale());
    }

    @Test
    void botaniasTiltLaysTheWingRoughlyFlat() {
        // Botania's texture is drawn on the diagonal; its -60 degree tilt drops the tip just below level.
        assertEquals(-15f, DivineWingLayout.tipElevationDegrees(BOTANIA_FLUGEL_TILT), 1e-4);
    }

    @Test
    void divineWingTipsPointDiagonallyUp() {
        float elevation = DivineWingLayout.tipElevationDegrees(wings.tilt());
        assertTrue(elevation >= 25f && elevation <= 50f, "tip elevation " + elevation);
    }

    @Test
    void wingRootSitsOnTheSpineWhateverTheSizeAndTilt() {
        // The texture's wing root is its bottom-right corner; after offset, rotation and scale it must land at x=0, y=0.
        for (float tilt : new float[]{-60f, -10f, 0f}) {
            for (float scale : new float[]{1.5f, 2.6f}) {
                float[] offset = DivineWingLayout.rootOffset(scale, tilt);
                double rad = Math.toRadians(tilt);
                double rootX = scale * Math.cos(rad) + offset[0];
                double rootY = scale * Math.sin(rad) + offset[1];
                assertEquals(0, rootX, 1e-4, "x at tilt " + tilt + " scale " + scale);
                assertEquals(0, rootY, 1e-4, "y at tilt " + tilt + " scale " + scale);
            }
        }
    }

    @Test
    void flyingFlapsHarderThanStanding() {
        float standingRange = 0, flyingRange = 0;
        for (int t = 0; t < 100; t++) {
            standingRange = Math.max(standingRange, Math.abs(DivineWingLayout.flap(t, false) - DivineWingLayout.flap(0, false)));
            flyingRange = Math.max(flyingRange, Math.abs(DivineWingLayout.flap(t, true) - DivineWingLayout.flap(0, true)));
        }
        assertTrue(flyingRange > standingRange);
    }

    @Test
    void haloHueCyclesAndEachCornerIsADifferentColour() {
        assertNotEquals(DivineWingLayout.haloHue(0f, 0), DivineWingLayout.haloHue(0f, 1));
        assertEquals(DivineWingLayout.haloHue(0f, 0), DivineWingLayout.haloHue(DivineWingLayout.HALO_CYCLE_TICKS, 0), 1e-5);
        for (int corner = 0; corner < 4; corner++) {
            float hue = DivineWingLayout.haloHue(37f, corner);
            assertTrue(hue >= 0 && hue < 1, "hue in [0,1): " + hue);
        }
    }
}
