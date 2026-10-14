package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthRegenTest {

    private final DivineArmorSettings settings = DivineArmorSettings.defaults();

    private Bonuses wearing(int pieces) {
        DivineLoadout.Builder b = DivineLoadout.builder();
        ArmorSlot[] slots = ArmorSlot.values();
        for (int i = 0; i < pieces; i++) b.wear(new DivinePiece(ArcanistClass.ARCANIST, slots[i]));
        return DivineBonuses.of(b.build(), settings);
    }

    @Test
    void healsOnlyOnThePulseTick() {
        HealthRegen regen = new HealthRegen(settings);
        int interval = settings.healthRegenIntervalTicks();

        assertEquals(0f, regen.healAt(interval - 1, wearing(4), 10f, 20f));
        assertEquals((float) (4 * settings.healthRegenPerPiece()), regen.healAt(interval, wearing(4), 10f, 20f), 1e-6);
    }

    @Test
    void neverHealsPastMaxHealth() {
        HealthRegen regen = new HealthRegen(settings);

        assertEquals(0.5f, regen.healAt(settings.healthRegenIntervalTicks(), wearing(4), 19.5f, 20f), 1e-6);
        assertEquals(0f, regen.healAt(settings.healthRegenIntervalTicks(), wearing(4), 20f, 20f));
    }

    @Test
    void noArmorNoHealing() {
        HealthRegen regen = new HealthRegen(settings);

        assertEquals(0f, regen.healAt(settings.healthRegenIntervalTicks(), wearing(0), 5f, 20f));
    }

    @Test
    void doesNotHealTheDead() {
        HealthRegen regen = new HealthRegen(settings);

        assertEquals(0f, regen.healAt(settings.healthRegenIntervalTicks(), wearing(4), 0f, 20f));
    }
}
