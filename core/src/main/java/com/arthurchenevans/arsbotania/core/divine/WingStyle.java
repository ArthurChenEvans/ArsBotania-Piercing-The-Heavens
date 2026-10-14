package com.arthurchenevans.arsbotania.core.divine;

/**
 * Wings shown by the Divine robes. Botania's styles keep the Flügel Tiara's variant numbers
 * so Botania's own renderer draws them; DIVINE is this mod's pearl-and-rainbow wing.
 */
public enum WingStyle {
    NONE(0),
    FLUGEL(1),
    ONE_WINGED_ANGEL(2),
    ICE_FAIRY(3),
    PHOENIX(4),
    BLACK_SNOW_PRINCESS(5),
    LORD_OF_THE_PIT(6),
    SYLPH(7),
    MEGA_ULTRA_CHICKEN(8),
    /** Botania's secret style, unlocked by name; never reached by cycling. */
    THE_ONE(9),
    DIVINE(10);

    public static final WingStyle DEFAULT = DIVINE;

    private final int id;

    WingStyle(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    /** The Flügel Tiara variant number, for styles Botania draws. */
    public int botaniaVariant() {
        return id;
    }

    public boolean isBotaniaWings() {
        return this != NONE && this != DIVINE;
    }

    /** Sneak-use order: Divine, no wings, then Botania's eight craftable styles. */
    public WingStyle next() {
        return switch (this) {
            case DIVINE -> NONE;
            case MEGA_ULTRA_CHICKEN, THE_ONE -> DIVINE;
            default -> fromId(id + 1);
        };
    }

    public static WingStyle fromId(int id) {
        for (WingStyle style : values()) {
            if (style.id == id) return style;
        }
        return DEFAULT;
    }
}
