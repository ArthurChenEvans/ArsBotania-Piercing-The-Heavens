package com.arthurchenevans.arsbotania.core.divine;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DivinePieceTest {

    @Test
    void thereAreTwelvePiecesThreeClassesTimesFourSlots() {
        List<DivinePiece> all = DivinePiece.all();

        assertEquals(12, all.size());
        assertEquals(12, new HashSet<>(all).size());
    }

    @Test
    void registryNamesMirrorTheArsNouveauArmorTheyAreMadeFrom() {
        Set<String> names = new HashSet<>();
        DivinePiece.all().forEach(p -> names.add(p.registryName()));

        assertTrue(names.containsAll(Set.of(
                "divine_sorcerer_hood", "divine_sorcerer_robes", "divine_sorcerer_leggings", "divine_sorcerer_boots",
                "divine_arcanist_hood", "divine_arcanist_robes", "divine_arcanist_leggings", "divine_arcanist_boots",
                "divine_battlemage_hood", "divine_battlemage_robes", "divine_battlemage_leggings", "divine_battlemage_boots")));
    }

    @Test
    void eachPieceKnowsTheTierThreeArsItemItUpgradesFrom() {
        DivinePiece robes = new DivinePiece(ArcanistClass.BATTLEMAGE, ArmorSlot.CHEST);

        assertEquals("ars_nouveau:battlemage_robes", robes.arsBaseItem());
    }

    @Test
    void displayNamesUseThePossessiveClassName() {
        assertEquals("Divine Sorcerer's Hood", new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.HEAD).displayName());
        assertEquals("Divine Battlemage's Boots", new DivinePiece(ArcanistClass.BATTLEMAGE, ArmorSlot.FEET).displayName());
    }
}
