package com.arthurchenevans.arsbotania.core.divine;

/**
 * The Divine Blade: the Terra Blade's numbers, with the abilities of the other top blades
 * folded in at their original strength. A convenience, not a stat boost.
 * (Spacetime will not bind to a weapon; that is the Singularity's alone.)
 */
public final class DivineBlade {
    /** Elementium Sword's pixie spawn chance bonus. */
    public static final double PIXIE_CHANCE = 0.05;
    private static final float PIXIE_BASE_DAMAGE = 4f;
    private static final float ELEMENTIUM_PIXIE_BONUS = 2f;

    public enum Ability {
        /** Botania Terra Blade: mana beam on a full swing. */
        TERRA_BEAM,
        /** Botania Thundercaller: chain lightning on hit. */
        CHAIN_LIGHTNING,
        /** Botania Starcaller: falling stars where you look as you swing. */
        FALLING_STARS,
        /** Botania Elementium Sword: more, stronger pixies. */
        PIXIES,
        /** Ars Nouveau Enchanter's Sword: its own spell, cast on hit. */
        SPELL_ON_HIT
    }

    private DivineBlade() {
    }

    public static boolean firesBeam(float attackStrength) {
        return attackStrength >= 1f;
    }

    public static float pixieDamage(boolean holdingBlade) {
        return PIXIE_BASE_DAMAGE + (holdingBlade ? ELEMENTIUM_PIXIE_BONUS : 0f);
    }

    public static boolean acceptsSpell(boolean spellHasCastMethod) {
        return !spellHasCastMethod;
    }
}
