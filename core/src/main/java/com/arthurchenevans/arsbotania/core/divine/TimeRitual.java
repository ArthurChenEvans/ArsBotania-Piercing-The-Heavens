package com.arthurchenevans.arsbotania.core.divine;

/**
 * The time component's overnight craft at the Runic Altar: it may only start at sunset,
 * mana must keep arriving all night (no gap longer than the allowed ticks) with the chunk
 * loaded, and it can only complete at sunrise. A failure loses the mana; the items stay.
 */
public final class TimeRitual {
    public static final int DUSK_START = 12_000;
    public static final int DUSK_END = 13_000;
    public static final int SUNRISE = 23_000;
    private static final int DAY = 24_000;

    public enum State { RUNNING, FAILED, READY }

    private final long sunriseGameTime;
    private long lastGain;
    private State state = State.RUNNING;

    private TimeRitual(long gameTime, long dayTime) {
        this.sunriseGameTime = gameTime + (SUNRISE - Math.floorMod(dayTime, DAY));
        this.lastGain = gameTime;
    }

    public static boolean canStart(long dayTime) {
        long t = Math.floorMod(dayTime, DAY);
        return t >= DUSK_START && t < DUSK_END;
    }

    /** Starts a ritual, or returns null if it is not sunset. */
    public static TimeRitual begin(long gameTime, long dayTime) {
        return canStart(dayTime) ? new TimeRitual(gameTime, dayTime) : null;
    }

    public long sunriseGameTime() {
        return sunriseGameTime;
    }

    public State state() {
        return state;
    }

    public State update(long gameTime, boolean manaArrived, boolean chunkLoaded, int maxGapTicks) {
        if (state != State.RUNNING) return state;
        if (!chunkLoaded) return state = State.FAILED;
        if (manaArrived) lastGain = gameTime;
        if (gameTime - lastGain > maxGapTicks) return state = State.FAILED;
        if (gameTime >= sunriseGameTime) state = State.READY;
        return state;
    }

    /** Until sunrise the altar is kept one mana short of finishing. */
    public static int manaCap(State state, int manaToGet) {
        return state == State.READY ? manaToGet : manaToGet - 1;
    }
}
