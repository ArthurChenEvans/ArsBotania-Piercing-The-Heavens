package com.arthurchenevans.arsbotania.core.divine;

import java.util.List;
import java.util.Optional;

/**
 * Divine sits outside every tier system: no ranks, no Ars tiers, no F-SS grade,
 * and nothing to level up or upgrade.
 */
public final class DivineTier {
    /** Divine pieces register a single Ars slot list, so the stored tier index is always this. */
    public static final int ARS_TIER_INDEX = 0;

    private DivineTier() {
    }

    public static Optional<String> tierLabel() {
        return Optional.empty();
    }

    public static boolean acceptsUpgrade() {
        return false;
    }

    public static int normaliseArsTier(int storedTier) {
        return ARS_TIER_INDEX;
    }

    public static List<Integer> threadSlots(DivineArmorSettings settings) {
        return settings.threadSlotLevels();
    }
}
