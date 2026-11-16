package com.arthurchenevans.arsbotania.core.divine;

/** The seven virtue runes, each the counterpart of one of Botania's sin runes. */
public enum VirtueRune {
    CHASTITY("chastity", "lust"),
    TEMPERANCE("temperance", "gluttony"),
    CHARITY("charity", "greed"),
    DILIGENCE("diligence", "sloth"),
    PATIENCE("patience", "wrath"),
    KINDNESS("kindness", "envy"),
    HUMILITY("humility", "pride");

    private final String virtue;
    private final String sin;

    VirtueRune(String virtue, String sin) {
        this.virtue = virtue;
        this.sin = sin;
    }

    public String registryName() {
        return "rune_of_" + virtue;
    }

    public String sin() {
        return sin;
    }

    public String sinRuneItem() {
        return "botania:rune_of_" + sin;
    }

    public static VirtueRune forSin(String sin) {
        for (VirtueRune v : values()) if (v.sin.equals(sin)) return v;
        throw new IllegalArgumentException("not a sin: " + sin);
    }
}
