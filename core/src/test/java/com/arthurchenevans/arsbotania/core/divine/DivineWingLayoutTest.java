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

    /** Where the wing root (the texture's bottom-right corner) ends up, mirroring the render transform. */
    private static double[] rootAfterTransform(float scale, float tilt, float[] offset) {
        // Item models are centred: the texture spans -0.5..0.5. Scale flips y.
        double x = 0.5 * scale, y = -0.5 * -scale;
        double rad = Math.toRadians(tilt);
        return new double[]{x * Math.cos(rad) - y * Math.sin(rad) + offset[0], x * Math.sin(rad) + y * Math.cos(rad) + offset[1]};
    }

    @Test
    void botaniasOwnPlacementPutsTheRootOnTheSpine() {
        // Botania: scale 1.5, tilt -60, a fixed one block shift. Lands within a few hundredths of the pivot.
        double[] root = rootAfterTransform(1.5f, -60f, new float[]{-1f, 0f});
        assertEquals(0, root[0], 0.05);
        assertEquals(0, Math.abs(root[1]), 0.3);
    }

    @Test
    void wingRootSitsOnTheSpineWhateverTheSizeAndTilt() {
        for (float tilt : new float[]{-60f, -10f, 0f, 20f}) {
            for (float scale : new float[]{1.5f, 2.6f}) {
                double[] root = rootAfterTransform(scale, tilt, DivineWingLayout.rootOffset(scale, tilt));
                assertEquals(0, root[0], 1e-4, "x at tilt " + tilt + " scale " + scale);
                assertEquals(0, root[1], 1e-4, "y at tilt " + tilt + " scale " + scale);
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
