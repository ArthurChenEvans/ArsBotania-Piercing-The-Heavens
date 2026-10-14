package com.arthurchenevans.arsbotania.core.divine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** One of the 12 Divine armor pieces. */
public record DivinePiece(ArcanistClass arcanistClass, ArmorSlot slot) {

    public DivinePiece {
        Objects.requireNonNull(arcanistClass, "arcanistClass");
        Objects.requireNonNull(slot, "slot");
    }

    public static List<DivinePiece> all() {
        List<DivinePiece> pieces = new ArrayList<>();
        for (ArcanistClass cls : ArcanistClass.values()) {
            for (ArmorSlot slot : ArmorSlot.values()) {
                pieces.add(new DivinePiece(cls, slot));
            }
        }
        return List.copyOf(pieces);
    }

    /** Path under this mod's namespace, e.g. {@code divine_sorcerer_hood}. */
    public String registryName() {
        return "divine_" + arcanistClass.id() + "_" + slot.suffix();
    }

    /** The Ars Nouveau item this piece is made from in the Enchanting Apparatus. */
    public String arsBaseItem() {
        return "ars_nouveau:" + arcanistClass.id() + "_" + slot.suffix();
    }

    public String displayName() {
        return "Divine " + arcanistClass.displayName() + "'s " + slot.displayName();
    }
}
