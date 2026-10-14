package com.arthurchenevans.arsbotania.core.divine;

/** Adds up what a loadout gives. A partial set gets each piece's own bonuses only. */
public final class DivineBonuses {
    private DivineBonuses() {
    }

    public static Bonuses of(DivineLoadout loadout, DivineArmorSettings s) {
        int pieces = loadout.count();
        if (pieces == 0) return Bonuses.NONE;

        int armor = 0;
        double pixieChance = 0;
        for (DivinePiece piece : loadout.pieces()) {
            armor += s.defence(piece.slot());
            pixieChance += s.pixieChance(piece.slot());
        }

        boolean full = loadout.isFullSet();
        double setDiscount = full ? setDiscount(s) : 0;
        double discount = Math.min(s.maxToolDiscount(), uncappedDiscount(loadout, s));

        return new Bonuses(
                full,
                armor,
                pieces * s.toughnessPerPiece(),
                pieces * s.botaniaManaRegenPerPiece(),
                discount,
                pieces * s.healthRegenPerPiece(),
                pieces * s.arsMaxManaPerPiece(),
                pieces * s.arsManaRegenPerPiece(),
                setDiscount,
                Math.min(1.0, pixieChance)
        );
    }

    /**
     * The discount one worn piece reports to Botania, which sums them across armor slots.
     * The full-set bonuses ride on the helmet so it is counted exactly once.
     */
    public static double discountReportedBy(ArmorSlot slot, DivineLoadout loadout, DivineArmorSettings s) {
        if (loadout.in(slot).isEmpty()) return 0;
        double discount = s.botaniaToolDiscountPerPiece();
        if (slot == ArmorSlot.HEAD && loadout.isFullSet()) discount += setDiscount(s);
        // Scale every report down by the same factor so Botania's sum lands on the cap.
        double total = uncappedDiscount(loadout, s);
        return total > s.maxToolDiscount() ? discount * s.maxToolDiscount() / total : discount;
    }

    private static double uncappedDiscount(DivineLoadout loadout, DivineArmorSettings s) {
        return loadout.count() * s.botaniaToolDiscountPerPiece() + (loadout.isFullSet() ? setDiscount(s) : 0);
    }

    private static double setDiscount(DivineArmorSettings s) {
        return s.terrasteelSetDiscount() + s.manaweaveSetDiscount();
    }
}
