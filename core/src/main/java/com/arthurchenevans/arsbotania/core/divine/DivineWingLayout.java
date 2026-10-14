package com.arthurchenevans.arsbotania.core.divine;

import java.util.List;

/**
 * How the Divine wings sit on the back: three pairs, archangel style, each bigger than
 * Botania's Flügel wings (scale 1.5), shrinking and dropping from top to bottom.
 */
public final class DivineWingLayout {
    /** Ticks for the halo's rainbow to go all the way round. */
    public static final float HALO_CYCLE_TICKS = 80f;

    /**
     * @param scale  wing size (Botania's Flügel is 1.5)
     * @param height offset up the back, in blocks
     * @param tilt   degrees the wing is rotated in its own plane (Botania uses -60)
     * @param phase  flap phase offset, in ticks, so pairs don't beat in unison
     */
    public record Pair(float scale, float height, float tilt, float phase) {
    }

    public static final List<Pair> PAIRS = List.of(
            new Pair(2.2f, 0.65f, -70f, 0f),
            new Pair(1.9f, 0.40f, -45f, 4f),
            new Pair(1.6f, 0.15f, -20f, 8f));

    private DivineWingLayout() {
    }

    /** Botania's Flügel flap formula, per pair. */
    public static float flap(Pair pair, float tickTime, boolean flying) {
        float t = tickTime + pair.phase();
        return 20 + ((float) Math.sin(t * (flying ? 0.4f : 0.2f)) + 0.5f) * (flying ? 30 : 5);
    }

    /** Rainbow hue for one corner of the halo; corners are a quarter turn apart so it shimmers. */
    public static float haloHue(float tickTime, int corner) {
        float hue = tickTime / HALO_CYCLE_TICKS + corner * 0.25f;
        return hue - (float) Math.floor(hue);
    }
}
