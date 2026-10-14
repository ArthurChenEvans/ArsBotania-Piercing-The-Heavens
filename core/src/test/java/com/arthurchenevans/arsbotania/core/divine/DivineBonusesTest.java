package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sociable tests: real settings, real loadout, real calculator - no doubles.
 */
class DivineBonusesTest {

    private final DivineArmorSettings defaults = DivineArmorSettings.defaults();

    private static DivineLoadout fullSet(ArcanistClass cls) {
        DivineLoadout.Builder b = DivineLoadout.builder();
        for (ArmorSlot slot : ArmorSlot.values()) b.wear(new DivinePiece(cls, slot));
        return b.build();
    }

    @Test
    void defaultDefenceMatchesTerrasteel() {
        Bonuses full = DivineBonuses.of(fullSet(ArcanistClass.SORCERER), defaults);

        // Terrasteel: helm 3, chest 8, legs 6, boots 3, toughness 3 per piece
        assertEquals(20, full.armor());
        assertEquals(12.0, full.toughness(), 1e-9);
    }

    @Test
    void emptyLoadoutGivesNothing() {
        Bonuses none = DivineBonuses.of(DivineLoadout.builder().build(), defaults);

        assertEquals(Bonuses.NONE, none);
        assertFalse(none.isFullSet());
    }

    @Test
    void partialSetGetsOnlyEachPiecesOwnBonuses() {
        DivineLoadout helmAndBoots = DivineLoadout.builder()
                .wear(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.HEAD))
                .wear(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.FEET))
                .build();

        Bonuses b = DivineBonuses.of(helmAndBoots, defaults);

        assertFalse(b.isFullSet());
        assertEquals(3 + 3, b.armor());
        assertEquals(2 * defaults.botaniaManaRegenPerPiece(), b.botaniaManaPerTick());
        assertEquals(2 * defaults.botaniaToolDiscountPerPiece(), b.botaniaToolDiscount(), 1e-9);
        assertEquals(2 * defaults.healthRegenPerPiece(), b.healthPerRegenPulse(), 1e-9);
        assertEquals(2 * defaults.arsMaxManaPerPiece(), b.arsMaxMana());
        assertEquals(2 * defaults.arsManaRegenPerPiece(), b.arsManaRegen(), 1e-9);
        assertEquals(0, b.fullSetExtraDiscount(), 1e-9);
    }

    @Test
    void fullSetAddsTheSetBonusOnTopOfThePieces() {
        Bonuses b = DivineBonuses.of(fullSet(ArcanistClass.BATTLEMAGE), defaults);

        assertTrue(b.isFullSet());
        assertEquals(4 * defaults.botaniaToolDiscountPerPiece() + defaults.fullSetBotaniaToolDiscount(),
                b.botaniaToolDiscount(), 1e-9);
    }

    @Test
    void mixingClassesStillCountsAsAFullDivineSet() {
        DivineLoadout mixed = DivineLoadout.builder()
                .wear(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.HEAD))
                .wear(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.CHEST))
                .wear(new DivinePiece(ArcanistClass.BATTLEMAGE, ArmorSlot.LEGS))
                .wear(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.FEET))
                .build();

        assertTrue(DivineBonuses.of(mixed, defaults).isFullSet());
    }

    @Test
    void toolDiscountNeverExceedsOneHundredPercent() {
        DivineArmorSettings greedy = defaults.toBuilder()
                .botaniaToolDiscountPerPiece(0.5)
                .fullSetBotaniaToolDiscount(0.5)
                .build();

        assertEquals(1.0, DivineBonuses.of(fullSet(ArcanistClass.SORCERER), greedy).botaniaToolDiscount(), 1e-9);
    }

    @Test
    void tunedSettingsFlowStraightThrough() {
        DivineArmorSettings tuned = defaults.toBuilder()
                .defence(ArmorSlot.CHEST, 20)
                .arsMaxManaPerPiece(1000)
                .build();
        DivineLoadout chestOnly = DivineLoadout.builder()
                .wear(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.CHEST)).build();

        Bonuses b = DivineBonuses.of(chestOnly, tuned);

        assertEquals(20, b.armor());
        assertEquals(1000, b.arsMaxMana());
    }

    @Test
    void wearingAPieceInTheWrongSlotIsRejected() {
        DivineLoadout.Builder b = DivineLoadout.builder();
        DivinePiece boots = new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.FEET);

        assertThrows(IllegalArgumentException.class, () -> b.wear(ArmorSlot.HEAD, boots));
    }

    @Test
    void perPieceDiscountsReportedToBotaniaAddUpToTheTotal() {
        // Botania asks every worn piece for its discount and sums them.
        DivineLoadout full = fullSet(ArcanistClass.ARCANIST);
        double sum = 0;
        for (ArmorSlot slot : ArmorSlot.values()) sum += DivineBonuses.discountReportedBy(slot, full, defaults);

        assertEquals(DivineBonuses.of(full, defaults).botaniaToolDiscount(), sum, 1e-9);
    }

    @Test
    void onlyTheHelmetCarriesTheFullSetDiscount() {
        DivineLoadout full = fullSet(ArcanistClass.ARCANIST);

        assertEquals(defaults.botaniaToolDiscountPerPiece() + defaults.fullSetBotaniaToolDiscount(),
                DivineBonuses.discountReportedBy(ArmorSlot.HEAD, full, defaults), 1e-9);
        assertEquals(defaults.botaniaToolDiscountPerPiece(),
                DivineBonuses.discountReportedBy(ArmorSlot.CHEST, full, defaults), 1e-9);
    }

    @Test
    void anEmptySlotReportsNoDiscount() {
        DivineLoadout chestOnly = DivineLoadout.builder()
                .wear(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.CHEST)).build();

        assertEquals(0, DivineBonuses.discountReportedBy(ArmorSlot.HEAD, chestOnly, defaults), 1e-9);
    }
}
