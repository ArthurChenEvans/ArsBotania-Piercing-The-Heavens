package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static com.arthurchenevans.arsbotania.core.divine.ComboOrder.Held.*;
import static org.junit.jupiter.api.Assertions.*;

/** Left hand acts first, then right: which hand holds which decides the combo. */
class ComboOrderTest {

    @Test
    void bookInLeftRodInRightIsSpellThenRod() {
        // right-handed: main hand is the right hand
        assertEquals(ComboOrder.SPELL_THEN_ROD, ComboOrder.of(true, ROD, GRIMOIRE));
    }

    @Test
    void rodInLeftBookInRightIsRodThenSpell() {
        assertEquals(ComboOrder.ROD_THEN_SPELL, ComboOrder.of(true, GRIMOIRE, ROD));
    }

    @Test
    void leftHandedPlayersStillGoLeftThenRight() {
        // left-handed: main hand is the left hand
        assertEquals(ComboOrder.SPELL_THEN_ROD, ComboOrder.of(false, GRIMOIRE, ROD));
        assertEquals(ComboOrder.ROD_THEN_SPELL, ComboOrder.of(false, ROD, GRIMOIRE));
    }

    @Test
    void anythingElseIsNoCombo() {
        assertEquals(ComboOrder.NONE, ComboOrder.of(true, GRIMOIRE, OTHER));
        assertEquals(ComboOrder.NONE, ComboOrder.of(true, ROD, ROD));
        assertEquals(ComboOrder.NONE, ComboOrder.of(true, OTHER, OTHER));
    }

    @Test
    void comboOrderListsItsSteps() {
        assertEquals(java.util.List.of(ComboOrder.Step.SPELL, ComboOrder.Step.ROD), ComboOrder.SPELL_THEN_ROD.steps());
        assertEquals(java.util.List.of(ComboOrder.Step.ROD, ComboOrder.Step.SPELL), ComboOrder.ROD_THEN_SPELL.steps());
    }
}
