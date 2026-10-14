package com.arthurchenevans.arsbotania.core.divine;

/**
 * The Divine Grimoire's shared mana pool: when Ars mana falls short for a spell, the shortfall
 * is bought from nearby Source jars, then from Botania mana, at configured rates. Nothing is free.
 */
public final class ManaBridge {
    private ManaBridge() {
    }

    /** Ars mana to top up so a spell costing {@code cost} can be cast; 0 if not needed or impossible. */
    public static int deficit(int cost, double current, int max) {
        if (cost > max || current >= cost) return 0;
        return (int) Math.ceil(Math.min(cost - current, max - current));
    }

    public static int botaniaCost(int arsMana, DivineArmorSettings s) {
        return arsMana * s.botaniaManaPerArsMana();
    }

    public static int sourceCost(int arsMana, DivineArmorSettings s) {
        return arsMana * s.sourcePerArsMana();
    }
}
