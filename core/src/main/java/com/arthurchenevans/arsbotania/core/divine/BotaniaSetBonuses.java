package com.arthurchenevans.arsbotania.core.divine;

/**
 * The Botania full-set bonuses Divine armor also carries, mirroring Botania's own rules:
 * Terrasteel's hungry regen and tablet mana, Manaweave's rod proficiency and
 * Elementium's potion-applying pixies. All need the full Divine set.
 */
public final class BotaniaSetBonuses {
    // Botania's Terrasteel helmet heals only when 0 < food < 18; vanilla regen covers the rest.
    private static final int MAX_FOOD_FOR_HUNGRY_REGEN = 17;

    private final DivineArmorSettings settings;

    public BotaniaSetBonuses(DivineArmorSettings settings) {
        this.settings = settings;
    }

    public float hungerRegenAt(long tickCount, int food, float health, float maxHealth, DivineLoadout loadout) {
        if (!loadout.isFullSet()) return 0f;
        if (food <= 0 || food > MAX_FOOD_FOR_HUNGRY_REGEN) return 0f;
        if (health <= 0 || health >= maxHealth) return 0f;
        if (tickCount % settings.terrasteelHungerRegenIntervalTicks() != 0) return 0f;
        return Math.min(settings.terrasteelHungerRegen(), maxHealth - health);
    }

    public int tabletManaAt(long tickCount, DivineLoadout loadout) {
        if (!loadout.isFullSet()) return 0;
        if (tickCount % settings.terrasteelTabletManaIntervalTicks() != 0) return 0;
        return settings.terrasteelTabletMana();
    }

    public boolean givesRodProficiency(DivineLoadout loadout) {
        return loadout.isFullSet();
    }

    public boolean pixiesApplyPotions(DivineLoadout loadout) {
        return loadout.isFullSet();
    }
}
