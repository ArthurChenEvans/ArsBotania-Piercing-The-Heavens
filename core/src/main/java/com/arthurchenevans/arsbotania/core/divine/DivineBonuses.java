package com.arthurchenevans.arsbotania.core.divine;

/** Adds up what a loadout gives. A partial set gets each piece's own bonuses only. */
public final class DivineBonuses {
    private DivineBonuses() {
    }

    public static Bonuses of(DivineLoadout loadout, DivineArmorSettings s) {
        int pieces = loadout.count();
        if (pieces == 0) return Bonuses.NONE;

        int armor = 0;
        for (DivinePiece piece : loadout.pieces()) {
            armor += s.defence(piece.slot());
        }

        boolean full = loadout.isFullSet();
        double setDiscount = full ? s.fullSetBotaniaToolDiscount() : 0;
        double discount = Math.min(1.0, pieces * s.botaniaToolDiscountPerPiece() + setDiscount);

        return new Bonuses(
                full,
                armor,
                pieces * s.toughnessPerPiece(),
                pieces * s.botaniaManaRegenPerPiece(),
                discount,
                pieces * s.healthRegenPerPiece(),
                pieces * s.arsMaxManaPerPiece(),
                pieces * s.arsManaRegenPerPiece(),
                setDiscount
        );
    }
}
