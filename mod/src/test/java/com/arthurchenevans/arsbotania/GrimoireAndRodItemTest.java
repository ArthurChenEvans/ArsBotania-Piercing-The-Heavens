package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.RodPower;
import com.arthurchenevans.arsbotania.item.DivineRodItem;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.spell.SpellTier;
import com.hollingsworth.arsnouveau.common.items.SpellBook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import vazkii.botania.common.item.BotaniaItems;

import static org.junit.jupiter.api.Assertions.*;

class GrimoireAndRodItemTest {

    @Test
    void grimoireIsAnArchmageSpellbookWithTenSlots() {
        ItemStack book = new ItemStack(DivineItems.DIVINE_GRIMOIRE.get());
        assertInstanceOf(SpellBook.class, book.getItem());
        assertEquals(SpellTier.THREE, ((SpellBook) book.getItem()).getTier());
        assertEquals(10, SpellCasterRegistry.from(book).getMaxSlots());
    }

    @Test
    void everyRodPowerIsARealBotaniaRod() {
        for (RodPower power : RodPower.values()) {
            assertTrue(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(power.botaniaItem())), power.botaniaItem());
        }
    }

    @Test
    void rodStartsOnTheLandsAndSwitchesPower() {
        ItemStack rod = new ItemStack(DivineItems.DIVINE_ROD.get());
        assertSame(BotaniaItems.ROD_OF_THE_LANDS, DivineRodItem.rod(rod));

        DivineRodItem.setPower(rod, RodPower.UNSTABLE_RESERVOIR);

        assertSame(BotaniaItems.ROD_OF_THE_UNSTABLE_RESERVOIR, DivineRodItem.rod(rod));
    }

    @Test
    void artShipsAnimated() {
        for (String tex : new String[]{"divine_grimoire", "divine_rod"}) {
            assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/" + tex + ".png"), tex);
            assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/" + tex + ".png.mcmeta"), tex);
        }
    }
}
