package com.arthurchenevans.arsbotania.core.divine;

import java.util.List;

/** What a critical hit does once the Divine hood's Ancient Wills are applied. */
public record CritOutcome(float damageMultiplier, float heal, boolean armorPiercing, List<Effect> effects) {
    public static final CritOutcome NONE = new CritOutcome(1f, 0f, false, List.of());

    public CritOutcome {
        effects = List.copyOf(effects);
    }

    public enum EffectType { WEAKNESS, SLOWNESS, WITHER }

    /** A potion effect applied to the target. */
    public record Effect(EffectType type, int durationTicks, int amplifier) {
    }
}
