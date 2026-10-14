package com.arthurchenevans.arsbotania.core.divine;

/** The three Ars Nouveau armor lines a Divine piece can be made from. */
public enum ArcanistClass {
    SORCERER("sorcerer", "Sorcerer"),
    ARCANIST("arcanist", "Arcanist"),
    BATTLEMAGE("battlemage", "Battlemage");

    private final String id;
    private final String displayName;

    ArcanistClass(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }
}
