package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Every mana and Source cost in the crafting chain is a config value. */
class RecipeCostTest {

    private final DivineArmorSettings s = DivineArmorSettings.defaults();

    @Test
    void defaultsMatchTheRequirements() {
        assertEquals(500_000, s.cost(RecipeCost.RUNE_OF_LIFE_AND_DEATH));
        assertEquals(1_000_000, s.cost(RecipeCost.YIN_YANG));
        assertEquals(10_000_000, s.cost(RecipeCost.SPACE_COMPONENT));
        assertEquals(10_000_000, s.cost(RecipeCost.TIME_COMPONENT));
        assertEquals(400_000_000, s.cost(RecipeCost.DIVINE_ICHOR));
        assertEquals(50_000, s.cost(RecipeCost.DIVINE_ARMOR));
    }

    @Test
    void costsSayWhichCurrencyTheyUse() {
        assertEquals(RecipeCost.Currency.BOTANIA_MANA, RecipeCost.DIVINE_ICHOR.currency());
        assertEquals(RecipeCost.Currency.SOURCE, RecipeCost.DIVINE_ARMOR.currency());
        assertEquals(RecipeCost.Currency.SOURCE, RecipeCost.SPACETIME_FABRIC.currency());
    }

    @Test
    void costsAreTunableAndPositive() {
        assertEquals(123, s.toBuilder().cost(RecipeCost.YIN_YANG, 123).build().cost(RecipeCost.YIN_YANG));
        assertThrows(IllegalArgumentException.class, () -> s.toBuilder().cost(RecipeCost.YIN_YANG, 0).build());
    }

    @Test
    void theDivineTankHoldsEnoughForTheArmorInfusion() {
        assertTrue(s.divineTankCapacity() >= s.cost(RecipeCost.DIVINE_ARMOR));
        assertTrue(s.divineTankCapacity() >= s.cost(RecipeCost.SPACETIME_FABRIC));
    }

    @Test
    void tankMustHoldTheBiggestSourceCost() {
        assertThrows(IllegalArgumentException.class,
                () -> s.toBuilder().divineTankCapacity(10).build());
    }
}
