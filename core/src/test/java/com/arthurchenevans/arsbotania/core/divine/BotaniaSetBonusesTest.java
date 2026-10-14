package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Divine also carries the Terrasteel, Manaweave and Elementium set bonuses. */
class BotaniaSetBonusesTest {

    private final DivineArmorSettings s = DivineArmorSettings.defaults();
    private final BotaniaSetBonuses bonuses = new BotaniaSetBonuses(s);

    private static DivineLoadout wearing(int pieces) {
        DivineLoadout.Builder b = DivineLoadout.builder();
        ArmorSlot[] slots = ArmorSlot.values();
        for (int i = 0; i < pieces; i++) b.wear(new DivinePiece(ArcanistClass.SORCERER, slots[i]));
        return b.build();
    }

    // Terrasteel: "Regen even if hunger isn't topped off"

    @Test
    void fullSetHealsWhileHungryOnTheTerrasteelPulse() {
        int pulse = s.terrasteelHungerRegenIntervalTicks();

        assertEquals(1f, bonuses.hungerRegenAt(pulse, 10, 10f, 20f, wearing(4)), 1e-6);
        assertEquals(0f, bonuses.hungerRegenAt(pulse + 1, 10, 10f, 20f, wearing(4)));
    }

    @Test
    void hungerRegenNeedsFoodAndAnInjury() {
        int pulse = s.terrasteelHungerRegenIntervalTicks();

        assertEquals(0f, bonuses.hungerRegenAt(pulse, 0, 10f, 20f, wearing(4)), "starving");
        assertEquals(0f, bonuses.hungerRegenAt(pulse, 18, 10f, 20f, wearing(4)), "vanilla regen covers full food");
        assertEquals(0f, bonuses.hungerRegenAt(pulse, 10, 20f, 20f, wearing(4)), "not hurt");
    }

    @Test
    void hungerRegenIsAFullSetBonus() {
        assertEquals(0f, bonuses.hungerRegenAt(s.terrasteelHungerRegenIntervalTicks(), 10, 10f, 20f, wearing(3)));
    }

    // Terrasteel: "Passive Mana generation on Mana Tablets"

    @Test
    void fullSetGeneratesTabletManaOnTheTerrasteelPulse() {
        int pulse = s.terrasteelTabletManaIntervalTicks();

        assertEquals(s.terrasteelTabletMana(), bonuses.tabletManaAt(pulse, wearing(4)));
        assertEquals(0, bonuses.tabletManaAt(pulse + 1, wearing(4)));
        assertEquals(0, bonuses.tabletManaAt(pulse, wearing(3)));
    }

    // Manaweave: "Increased proficiency with Magical Rods"; Elementium: potion pixies

    @Test
    void rodProficiencyAndPotionPixiesNeedTheFullSet() {
        assertTrue(bonuses.givesRodProficiency(wearing(4)));
        assertFalse(bonuses.givesRodProficiency(wearing(3)));
        assertTrue(bonuses.pixiesApplyPotions(wearing(4)));
        assertFalse(bonuses.pixiesApplyPotions(wearing(3)));
    }

    // Discounts: Terrasteel 20% + Manaweave 40% on a full set

    @Test
    void fullSetCarriesTheTerrasteelAndManaweaveDiscounts() {
        Bonuses full = DivineBonuses.of(wearing(4), s);

        assertEquals(0.2, s.terrasteelSetDiscount(), 1e-9);
        assertEquals(0.4, s.manaweaveSetDiscount(), 1e-9);
        // Together they exceed the 50% cap, so a full set lands exactly on it.
        assertEquals(s.maxToolDiscount(), full.botaniaToolDiscount(), 1e-9);
    }

    @Test
    void partialSetGetsNoSetDiscount() {
        assertEquals(3 * s.botaniaToolDiscountPerPiece(), DivineBonuses.of(wearing(3), s).botaniaToolDiscount(), 1e-9);
    }

    // Elementium: pixie spawn chance per piece

    @Test
    void pixieChanceDefaultsMatchElementiumPerPiece() {
        assertEquals(0.11, s.pixieChance(ArmorSlot.HEAD), 1e-9);
        assertEquals(0.17, s.pixieChance(ArmorSlot.CHEST), 1e-9);
        assertEquals(0.15, s.pixieChance(ArmorSlot.LEGS), 1e-9);
        assertEquals(0.09, s.pixieChance(ArmorSlot.FEET), 1e-9);
    }

    @Test
    void pixieChanceIsSummedPerWornPiece() {
        assertEquals(0.11 + 0.17, DivineBonuses.of(wearing(2), s).pixieChance(), 1e-9);
    }

    @Test
    void pixieChanceMustBeAProbability() {
        assertThrows(IllegalArgumentException.class, () -> s.toBuilder().pixieChance(ArmorSlot.HEAD, 1.5).build());
    }
}
