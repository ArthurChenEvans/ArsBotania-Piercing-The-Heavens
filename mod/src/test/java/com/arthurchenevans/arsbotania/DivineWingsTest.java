package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.WingStyle;
import com.arthurchenevans.arsbotania.item.DivineRobesItem;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The Divine robes carry the Flügel Tiara: flight plus a chosen wing style. */
class DivineWingsTest {

    @Test
    void everyClassesRobesAreWingedAndNothingElseIs() {
        for (DivinePiece piece : DivinePiece.all()) {
            boolean robes = piece.slot() == ArmorSlot.CHEST;
            assertEquals(robes, DivineItems.get(piece) instanceof DivineRobesItem, piece.registryName());
        }
    }

    @Test
    void newRobesShowDivineWings() {
        ItemStack robes = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.CHEST)));

        assertEquals(WingStyle.DIVINE, DivineRobesItem.wingStyle(robes));
    }

    @Test
    void wingStyleIsStoredOnTheStack() {
        ItemStack robes = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.CHEST)));

        DivineRobesItem.setWingStyle(robes, WingStyle.PHOENIX);

        assertEquals(WingStyle.PHOENIX, DivineRobesItem.wingStyle(robes));
    }

    @Test
    void divineWingArtShipsAnimated() {
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/divine_wings.png"));
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/divine_wings.png.mcmeta"));
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/models/item/divine_wings.json"));
    }
}
