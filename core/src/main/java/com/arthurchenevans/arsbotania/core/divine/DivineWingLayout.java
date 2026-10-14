package com.arthurchenevans.arsbotania.core.divine;

/**
 * How the Divine wings sit on the back: one big pair, tips angled diagonally upward.
 *
 * <p>Botania's wing texture is drawn on the diagonal (root bottom-right, tip top-left).
 * Botania tilts it -60 degrees so the wing lies roughly flat; tilting it less keeps the
 * diagonal, raising the tip {@code 45 + tilt} degrees above horizontal.
 */
public final class DivineWingLayout {
    /** Ticks for the halo's rainbow to go all the way round. */
    public static final float HALO_CYCLE_TICKS = 80f;

    /**
     * @param scale  wing size (Botania's Flügel is 1.5)
     * @param height how far down the back from the neck the root sits, in blocks
     * @param tilt   degrees the wing is rotated in its own plane (Botania uses -60)
     */
    public record Pair(float scale, float height, float tilt) {
    }

    public static final Pair WINGS = new Pair(2.6f, 0.3f, -10f);

    private DivineWingLayout() {
    }

    /** Degrees above horizontal the wing tip points for a given tilt. */
    public static float tipElevationDegrees(float tilt) {
        return 45f + tilt;
    }

    /**
     * Translation (applied before rotation and scale) that puts the wing's root, the texture's
     * bottom-right corner, on the spine. Botania's fixed -1 only lines up at its own size.
     */
    public static float[] rootOffset(float scale, float tilt) {
        double rad = Math.toRadians(tilt);
        return new float[]{(float) (-scale * Math.cos(rad)), (float) (-scale * Math.sin(rad))};
    }

    /** Botania's Flügel flap formula. */
    public static float flap(float tickTime, boolean flying) {
        return 20 + ((float) Math.sin(tickTime * (flying ? 0.4f : 0.2f)) + 0.5f) * (flying ? 30 : 5);
    }

    /** Rainbow hue for one corner of the halo; corners are a quarter turn apart so it shimmers. */
    public static float haloHue(float tickTime, int corner) {
        float hue = tickTime / HALO_CYCLE_TICKS + corner * 0.25f;
        return hue - (float) Math.floor(hue);
    }
}
