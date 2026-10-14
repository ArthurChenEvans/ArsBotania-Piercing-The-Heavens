package com.arthurchenevans.arsbotania.core.divine;

/** Botania's rods, as the Divine Rod's selectable powers. */
public enum RodPower {
    LANDS("rod_of_the_lands"),
    HIGHLANDS("rod_of_the_highlands"),
    DEPTHS("rod_of_the_depths"),
    TERRA_FIRMA("rod_of_the_terra_firma"),
    SHIFTING_CRUST("rod_of_the_shifting_crust"),
    SEAS("rod_of_the_seas"),
    HELLS("rod_of_the_hells"),
    MOLTEN_CORE("rod_of_the_molten_core"),
    SKIES("rod_of_the_skies"),
    BIFROST("rod_of_the_bifrost"),
    SHADED_MESA("rod_of_the_shaded_mesa"),
    PLENTIFUL_MANTLE("rod_of_the_plentiful_mantle"),
    /** Botania's registry name really is spelled "reservior". */
    UNSTABLE_RESERVOIR("rod_of_the_unstable_reservior");

    public static final RodPower DEFAULT = LANDS;

    private final String path;

    RodPower(String path) {
        this.path = path;
    }

    public String botaniaItem() {
        return "botania:" + path;
    }

    public RodPower next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static RodPower fromIndex(int index) {
        return index >= 0 && index < values().length ? values()[index] : DEFAULT;
    }
}
