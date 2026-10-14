package com.arthurchenevans.arsbotania.core.divine;

/** Decides how much health Divine armor restores on a given game tick. */
public final class HealthRegen {
    private final DivineArmorSettings settings;

    public HealthRegen(DivineArmorSettings settings) {
        this.settings = settings;
    }

    public float healAt(long gameTime, Bonuses bonuses, float health, float maxHealth) {
        if (health <= 0 || health >= maxHealth) return 0f;
        if (gameTime % settings.healthRegenIntervalTicks() != 0) return 0f;
        return (float) Math.min(bonuses.healthPerRegenPulse(), maxHealth - health);
    }
}
