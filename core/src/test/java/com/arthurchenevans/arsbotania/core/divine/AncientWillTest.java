package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Terrasteel's Ancient Wills, applied by the Divine hood on critical hits. */
class AncientWillTest {

    private static CritOutcome crit(Set<AncientWill> wills, boolean fullSet, float health) {
        return AncientWill.onCrit(wills, fullSet, health, 20f, 8f);
    }

    @Test
    void willIdsMatchBotania() {
        assertEquals("dharok", AncientWill.DHAROK.id());
        assertEquals(6, AncientWill.values().length);
    }

    @Test
    void noWillsNoEffect() {
        assertEquals(CritOutcome.NONE, crit(EnumSet.noneOf(AncientWill.class), true, 10f));
    }

    @Test
    void willsNeedTheFullSet() {
        assertEquals(CritOutcome.NONE, crit(EnumSet.allOf(AncientWill.class), false, 10f));
    }

    @Test
    void dharokScalesCritDamageWithMissingHealth() {
        assertEquals(1.25f, crit(EnumSet.of(AncientWill.DHAROK), true, 10f).damageMultiplier(), 1e-6);
        assertEquals(1.0f, crit(EnumSet.of(AncientWill.DHAROK), true, 20f).damageMultiplier(), 1e-6);
    }

    @Test
    void guthanHealsAQuarterOfTheDamage() {
        assertEquals(2f, crit(EnumSet.of(AncientWill.GUTHAN), true, 10f).heal(), 1e-6);
    }

    @Test
    void veracPiercesArmor() {
        assertTrue(crit(EnumSet.of(AncientWill.VERAC), true, 10f).armorPiercing());
        assertFalse(crit(EnumSet.of(AncientWill.GUTHAN), true, 10f).armorPiercing());
    }

    @Test
    void ahrimToragKarilApplyBotaniasEffects() {
        CritOutcome out = crit(EnumSet.of(AncientWill.AHRIM, AncientWill.TORAG, AncientWill.KARIL), true, 10f);

        assertEquals(Set.of(
                new CritOutcome.Effect(CritOutcome.EffectType.WEAKNESS, 20, 1),
                new CritOutcome.Effect(CritOutcome.EffectType.SLOWNESS, 60, 1),
                new CritOutcome.Effect(CritOutcome.EffectType.WITHER, 60, 1)), Set.copyOf(out.effects()));
    }

    @Test
    void parsesStoredWillIdsAndIgnoresUnknownOnes() {
        assertEquals(EnumSet.of(AncientWill.AHRIM, AncientWill.KARIL),
                AncientWill.parse(java.util.List.of("ahrim", "karil", "nonsense")));
    }
}
