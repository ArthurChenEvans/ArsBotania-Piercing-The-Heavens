package com.arthurchenevans.arsbotania.core.divine;

import java.util.List;

/**
 * Divine Grimoire + Divine Rod combos: the left hand acts first, then the right.
 * Book left, rod right: the spell goes off, then the rod acts where it landed.
 * Rod left, book right: the rod acts, then the spell lands where the rod hit.
 */
public enum ComboOrder {
    NONE(List.of()),
    SPELL_THEN_ROD(List.of(Step.SPELL, Step.ROD)),
    ROD_THEN_SPELL(List.of(Step.ROD, Step.SPELL));

    public enum Held { GRIMOIRE, ROD, OTHER }

    public enum Step { SPELL, ROD }

    private final List<Step> steps;

    ComboOrder(List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> steps() {
        return steps;
    }

    /**
     * @param mainArmIsRight the player's handedness setting (main hand is the right hand)
     */
    public static ComboOrder of(boolean mainArmIsRight, Held mainHand, Held offHand) {
        Held left = mainArmIsRight ? offHand : mainHand;
        Held right = mainArmIsRight ? mainHand : offHand;
        if (left == Held.GRIMOIRE && right == Held.ROD) return SPELL_THEN_ROD;
        if (left == Held.ROD && right == Held.GRIMOIRE) return ROD_THEN_SPELL;
        return NONE;
    }
}
