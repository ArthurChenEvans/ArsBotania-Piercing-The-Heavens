package com.arthurchenevans.arsbotania.core.divine;

/** Armor slots, with the item suffix Ars Nouveau uses for each. */
public enum ArmorSlot {
    HEAD("hood", "Hood"),
    CHEST("robes", "Robes"),
    LEGS("leggings", "Leggings"),
    FEET("boots", "Boots");

    private final String suffix;
    private final String displayName;

    ArmorSlot(String suffix, String displayName) {
        this.suffix = suffix;
        this.displayName = displayName;
    }

    public String suffix() {
        return suffix;
    }

    public String displayName() {
        return displayName;
    }
}
