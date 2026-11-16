package com.arthurchenevans.arsbotania.core.divine;

/** The crafting chain's costs. Each is a config value; these are the defaults. */
public enum RecipeCost {
    RUNE_OF_LIFE_AND_DEATH(Currency.BOTANIA_MANA, 500_000),
    YIN_YANG(Currency.BOTANIA_MANA, 1_000_000),
    SPACE_COMPONENT(Currency.BOTANIA_MANA, 10_000_000),
    TIME_COMPONENT(Currency.BOTANIA_MANA, 10_000_000),
    /** Undecided in the requirements; a placeholder default. */
    SPACETIME_FABRIC(Currency.SOURCE, 100_000),
    DIVINE_ICHOR(Currency.BOTANIA_MANA, 400_000_000),
    DIVINE_ARMOR(Currency.SOURCE, 50_000);

    public enum Currency { BOTANIA_MANA, SOURCE }

    private final Currency currency;
    private final int defaultCost;

    RecipeCost(Currency currency, int defaultCost) {
        this.currency = currency;
        this.defaultCost = defaultCost;
    }

    public Currency currency() {
        return currency;
    }

    public int defaultCost() {
        return defaultCost;
    }

    /** Lowercase key used in recipe files and the config. */
    public String key() {
        return name().toLowerCase();
    }

    public static RecipeCost fromKey(String key) {
        return valueOf(key.toUpperCase());
    }
}
