package com.arthurchenevans.arsbotania.core.divine;

/**
 * Totals granted by the Divine pieces currently worn.
 *
 * @param botaniaManaPerTick   Botania mana pushed into the wearer's mana items each tick
 * @param botaniaToolDiscount  fraction (0..1) taken off Botania tool mana costs
 * @param healthPerRegenPulse  health restored every regen pulse
 * @param fullSetExtraDiscount the part of the discount that comes from the full-set bonus
 */
public record Bonuses(
        boolean isFullSet,
        int armor,
        double toughness,
        int botaniaManaPerTick,
        double botaniaToolDiscount,
        double healthPerRegenPulse,
        int arsMaxMana,
        double arsManaRegen,
        double fullSetExtraDiscount
) {
    public static final Bonuses NONE = new Bonuses(false, 0, 0, 0, 0, 0, 0, 0, 0);
}
