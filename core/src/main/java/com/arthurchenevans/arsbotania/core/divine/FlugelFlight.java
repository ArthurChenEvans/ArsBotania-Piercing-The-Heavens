package com.arthurchenevans.arsbotania.core.divine;

/** Botania's Flügel Tiara flight rules, numbers unchanged, for the Divine robes. */
public final class FlugelFlight {
    public static final int MAX_FLIGHT_TIME = 1200;
    public static final int COST = 35;
    public static final int COST_OVERKILL = COST * 3;
    public static final int DASH_COOLDOWN = 40;
    private static final double GLIDE_START_SPEED = -0.7;

    private FlugelFlight() {
    }

    /** Mana per tick while flying; triples once the flight bar is empty. */
    public static int cost(int remaining) {
        return remaining <= 0 ? COST_OVERKILL : COST;
    }

    /**
     * Taking off needs more than a tenth of the bar; staying up needs any time at all.
     * The Eye of the Flügel ignores the bar. Mana for this tick is always required.
     */
    public static boolean canFly(int remaining, boolean wasFlying, boolean hasEyeOfFlugel, boolean manaAvailable) {
        boolean enoughTime = remaining > (wasFlying ? 0 : MAX_FLIGHT_TIME / 10) || hasEyeOfFlugel;
        return enoughTime && manaAvailable;
    }

    /** Flight bar after this tick: drains while flying, refills on the ground (slower while gliding). */
    public static int nextRemaining(int remaining, boolean flying, boolean creative, boolean gliding, long tickCount) {
        if (flying) {
            return remaining > 0 && !creative ? remaining - 1 : remaining;
        }
        if (remaining < MAX_FLIGHT_TIME && tickCount % (gliding ? 6 : 2) == 0) {
            return remaining + 1;
        }
        return remaining;
    }

    public static boolean shouldGlide(boolean sneaking, boolean onGround, double verticalSpeed, boolean wasGliding) {
        return sneaking && !onGround && (verticalSpeed < GLIDE_START_SPEED || wasGliding);
    }
}
