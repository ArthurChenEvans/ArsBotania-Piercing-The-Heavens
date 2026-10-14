package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DivineTierTest {

    private final DivineArmorSettings settings = DivineArmorSettings.defaults();

    @Test
    void divineItemsHaveNoTierLabel() {
        assertFalse(DivineTier.tierLabel().isPresent(), "no rank, no Ars tier, no F-SS grade");
    }

    @Test
    void divineItemsCannotBeUpgradedFurther() {
        assertFalse(DivineTier.acceptsUpgrade());
    }

    @Test
    void storedArsTierIsAlwaysPinnedToTheSingleDivineEntry() {
        // Ars stores a tier index on every perk holder; Divine registers exactly one slot list
        // and pins the index to it no matter what an upgrade recipe or old save wrote.
        assertEquals(0, DivineTier.normaliseArsTier(2));
        assertEquals(0, DivineTier.normaliseArsTier(-1));
    }

    @Test
    void threadSlotsComeFromConfig() {
        assertEquals(settings.threadSlotLevels(), DivineTier.threadSlots(settings));
        DivineArmorSettings four = settings.toBuilder().threadSlotLevels(java.util.List.of(3, 3, 3, 3)).build();
        assertEquals(4, DivineTier.threadSlots(four).size());
    }
}
