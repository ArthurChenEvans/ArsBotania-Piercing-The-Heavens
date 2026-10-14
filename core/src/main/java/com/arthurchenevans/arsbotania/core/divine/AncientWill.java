package com.arthurchenevans.arsbotania.core.divine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Botania's Ancient Wills. Ids match what Botania stores on a helmet. */
public enum AncientWill {
    AHRIM, DHAROK, GUTHAN, TORAG, VERAC, KARIL;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Set<AncientWill> parse(Collection<String> ids) {
        Set<AncientWill> wills = EnumSet.noneOf(AncientWill.class);
        for (AncientWill will : values()) {
            if (ids.contains(will.id())) wills.add(will);
        }
        return wills;
    }

    /** Botania's Terrasteel will effects, numbers included. Only a full set unlocks them. */
    public static CritOutcome onCrit(Set<AncientWill> wills, boolean fullSet, float health, float maxHealth, float damage) {
        if (!fullSet || wills.isEmpty()) return CritOutcome.NONE;

        float multiplier = wills.contains(DHAROK) ? 1f + (1f - health / maxHealth) * 0.5f : 1f;
        float heal = wills.contains(GUTHAN) ? damage * 0.25f : 0f;
        List<CritOutcome.Effect> effects = new ArrayList<>();
        if (wills.contains(AHRIM)) effects.add(new CritOutcome.Effect(CritOutcome.EffectType.WEAKNESS, 20, 1));
        if (wills.contains(TORAG)) effects.add(new CritOutcome.Effect(CritOutcome.EffectType.SLOWNESS, 60, 1));
        if (wills.contains(KARIL)) effects.add(new CritOutcome.Effect(CritOutcome.EffectType.WITHER, 60, 1));
        return new CritOutcome(multiplier, heal, wills.contains(VERAC), effects);
    }
}
