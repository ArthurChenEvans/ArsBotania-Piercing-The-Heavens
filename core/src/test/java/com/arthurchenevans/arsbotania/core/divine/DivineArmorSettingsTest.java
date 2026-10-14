package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DivineArmorSettingsTest {

    @Test
    void negativeValuesAreRejected() {
        var b = DivineArmorSettings.defaults().toBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.botaniaManaRegenPerPiece(-1).build());
    }

    @Test
    void discountMustBeAFraction() {
        var b = DivineArmorSettings.defaults().toBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.botaniaToolDiscountPerPiece(1.5).build());
    }

    @Test
    void regenIntervalMustBePositive() {
        var b = DivineArmorSettings.defaults().toBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.healthRegenIntervalTicks(0).build());
    }

    @Test
    void threadSlotLevelsMustBeArsSlotLevels() {
        var b = DivineArmorSettings.defaults().toBuilder();

        assertThrows(IllegalArgumentException.class, () -> b.threadSlotLevels(List.of(4)).build());
        assertThrows(IllegalArgumentException.class, () -> b.threadSlotLevels(List.of(0)).build());
    }

    @Test
    void temporaryRecipeCostIsTunable() {
        DivineArmorSettings s = DivineArmorSettings.defaults().toBuilder().temporaryRecipeSourceCost(123).build();

        assertEquals(123, s.temporaryRecipeSourceCost());
    }
}
