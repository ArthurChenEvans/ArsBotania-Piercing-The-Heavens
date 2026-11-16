package com.arthurchenevans.arsbotania.core.divine;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Michael, the archangel boss. Summoned at an active beacon; on death, everyone who hurt
 * him in the last 30 seconds and carries all seven sin runes has one of each turned into
 * its virtue.
 */
public final class Michael {

    public enum Attack { TELEPORT, MISSILES, LIGHTNING }

    public enum Phase {
        ONE(EnumSet.of(Attack.TELEPORT, Attack.MISSILES)),
        TWO(EnumSet.of(Attack.TELEPORT, Attack.MISSILES, Attack.LIGHTNING));

        private final Set<Attack> attacks;

        Phase(Set<Attack> attacks) {
            this.attacks = attacks;
        }

        public Set<Attack> attacks() {
            return attacks;
        }
    }

    private Michael() {
    }

    public static Phase phase(float health, float maxHealth) {
        return health > maxHealth / 2 ? Phase.ONE : Phase.TWO;
    }

    /** @param sinsCarried names of the sin runes the player carries ("lust", "pride", ...) */
    public static boolean earnsVirtues(long lastHitTick, long deathTick, int windowTicks, Set<String> sinsCarried) {
        return deathTick - lastHitTick <= windowTicks && sinsCarried.containsAll(conversion().keySet());
    }

    /** Sin rune name to its virtue, one of each converted. */
    public static Map<String, VirtueRune> conversion() {
        Map<String, VirtueRune> map = new LinkedHashMap<>();
        for (VirtueRune v : VirtueRune.values()) map.put(v.sin(), v);
        return map;
    }

    public static boolean canSummon(int beaconLevels, boolean michaelAlreadyThere) {
        return beaconLevels > 0 && !michaelAlreadyThere;
    }
}
