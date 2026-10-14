package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.item.DivineArmorItem;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.junit.jupiter.api.Test;
import vazkii.botania.api.mana.ManaDiscountArmor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sociable tests run inside a bootstrapped NeoForge with Ars Nouveau and Botania loaded:
 * real registries, real Ars perk holders and recipes, real Botania interfaces.
 */
class DivineArmorItemTest {

    private final DivineArmorSettings settings = DivineArmorSettings.defaults();

    private static double total(ItemAttributeModifiers mods, Holder<Attribute> attribute) {
        return mods.modifiers().stream()
                .filter(e -> e.attribute().equals(attribute))
                .mapToDouble(e -> e.modifier().amount())
                .sum();
    }

    @Test
    void allTwelvePiecesAreRegisteredUnderTheModNamespace() {
        for (DivinePiece piece : DivinePiece.all()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ArsBotania.MODID, piece.registryName());
            assertTrue(BuiltInRegistries.ITEM.containsKey(id), "missing " + id);
            assertInstanceOf(DivineArmorItem.class, BuiltInRegistries.ITEM.get(id));
        }
    }

    @Test
    void attributesComeFromSettings() {
        ItemStack robes = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.BATTLEMAGE, ArmorSlot.CHEST)));

        ItemAttributeModifiers mods = robes.getAttributeModifiers();

        assertEquals(settings.defence(ArmorSlot.CHEST), total(mods, Attributes.ARMOR), 1e-9);
        assertEquals(settings.toughnessPerPiece(), total(mods, Attributes.ARMOR_TOUGHNESS), 1e-9);
        assertEquals(settings.arsMaxManaPerPiece(), total(mods, PerkAttributes.MAX_MANA), 1e-9);
        assertEquals(settings.arsManaRegenPerPiece(), total(mods, PerkAttributes.MANA_REGEN_BONUS), 1e-9);
    }

    @Test
    void piecesAreBotaniaDiscountArmor() {
        for (DivinePiece piece : DivinePiece.all()) {
            assertInstanceOf(ManaDiscountArmor.class, DivineItems.get(piece));
        }
    }

    @Test
    void piecesHaveArsThreadSlotsFromSettings() {
        ItemStack hood = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.HEAD)));

        List<PerkSlot> slots = PerkUtil.getPerkHolder(hood).getSlotsForTier(hood);

        assertEquals(settings.threadSlotLevels().size(), slots.size());
        slots.forEach(s -> assertEquals(3, s.value()));
    }

    @Test
    void piecesNeverBreak() {
        ItemStack boots = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.FEET)));

        assertFalse(boots.isDamageableItem());
    }

    @Test
    void arsArmorUpgradeRecipesRejectDivinePieces() {
        ItemStack divine = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.CHEST)));
        ArmorUpgradeRecipe toTierTwo = new ArmorUpgradeRecipe(List.of(), 0, 1);

        assertFalse(toTierTwo.doesReagentMatch(new ApparatusRecipeInput(divine, List.of(), null), null, null));
    }

    @Test
    void arsArmorUpgradeStillWorksOnOrdinaryArsArmor() {
        ItemStack ordinary = new ItemStack(ItemsRegistry.ARCANIST_ROBES.get());
        ArmorUpgradeRecipe toTierTwo = new ArmorUpgradeRecipe(List.of(), 0, 1);

        assertTrue(toTierTwo.doesReagentMatch(new ApparatusRecipeInput(ordinary, List.of(), null), null, null));
    }

    @Test
    void storedTierIsPinnedBackToDivineOnLoad() {
        ItemStack legs = new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.LEGS)));
        legs.set(DataComponentRegistry.ARMOR_PERKS, PerkUtil.getPerkHolder(legs).setTier(2));

        legs.getItem().verifyComponentsAfterLoad(legs);

        assertEquals(0, PerkUtil.getPerkHolder(legs).getTier());
    }
}
