package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.item.DivineArmorItem;
import com.arthurchenevans.arsbotania.item.DivineSetTooltip;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import vazkii.botania.api.item.AncientWillContainer;
import vazkii.botania.api.item.ManaProficiencyArmor;
import vazkii.botania.common.handler.PixieHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Terrasteel, Elementium and Manaweave bonuses wired onto the real items, with Botania loaded. */
class BotaniaSetBonusItemTest {

    private final DivineArmorSettings settings = DivineArmorSettings.defaults();

    private static ItemStack stack(ArmorSlot slot) {
        return new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.ARCANIST, slot)));
    }

    private static List<String> keys(List<Component> lines) {
        List<String> keys = new ArrayList<>();
        for (Component c : lines) {
            if (c.getContents() instanceof TranslatableContents t) keys.add(t.getKey());
            c.getSiblings().forEach(s -> {
                if (s.getContents() instanceof TranslatableContents t) keys.add(t.getKey());
            });
        }
        return keys;
    }

    @Test
    void everyPieceCarriesItsElementiumPixieChance() {
        for (ArmorSlot slot : ArmorSlot.values()) {
            double chance = stack(slot).getAttributeModifiers().modifiers().stream()
                    .filter(e -> e.attribute().equals(PixieHandler.PIXIE_SPAWN_CHANCE))
                    .mapToDouble(e -> e.modifier().amount()).sum();
            assertEquals(settings.pixieChance(slot), chance, 1e-9, slot.toString());
        }
    }

    @Test
    void onlyTheHoodHoldsAncientWills() {
        assertInstanceOf(AncientWillContainer.class, DivineItems.get(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.HEAD)));
        assertFalse(DivineItems.get(new DivinePiece(ArcanistClass.SORCERER, ArmorSlot.CHEST)) instanceof AncientWillContainer);
    }

    @Test
    void hoodStoresWillsTheBotaniaWay() {
        ItemStack hood = stack(ArmorSlot.HEAD);
        AncientWillContainer container = (AncientWillContainer) hood.getItem();

        container.addAncientWill(hood, AncientWillContainer.AncientWillType.KARIL);

        assertTrue(container.hasAncientWill(hood, AncientWillContainer.AncientWillType.KARIL));
        assertFalse(container.hasAncientWill(hood, AncientWillContainer.AncientWillType.AHRIM));
    }

    @Test
    void piecesGiveManaweaveRodProficiency() {
        for (DivineArmorItem item : DivineItems.all()) {
            assertInstanceOf(ManaProficiencyArmor.class, item);
        }
    }

    @Test
    void setTooltipListsTerrasteelManaweaveAndElementiumBonuses() {
        List<String> keys = keys(DivineSetTooltip.lines(stack(ArmorSlot.CHEST), 2));

        assertTrue(keys.containsAll(List.of(
                "arsbotania.armorset.divine.discount",
                "botania.armorset.terrasteel.desc1",
                "botania.armorset.terrasteel.desc2",
                "botania.armorset.manaweave.desc1",
                "botania.armorset.elementium.desc")), keys.toString());
    }

    @Test
    void setTooltipShowsTheCappedDiscountNotBotaniasSeparateOnes() {
        List<Component> lines = DivineSetTooltip.lines(stack(ArmorSlot.CHEST), 4);
        List<String> keys = keys(lines);

        assertFalse(keys.contains("botania.armorset.terrasteel.desc0"), "Botania's 20% line no longer applies");
        assertFalse(keys.contains("botania.armorset.manaweave.desc0"), "Botania's 40% line no longer applies");
        TranslatableContents discount = lines.stream()
                .map(c -> findKey(c, "arsbotania.armorset.divine.discount"))
                .filter(java.util.Objects::nonNull).findFirst().orElseThrow();
        assertArrayEquals(new Object[]{"50"}, discount.getArgs());
    }

    @Test
    void setTooltipShowsHowManyPiecesAreWorn() {
        List<Component> lines = DivineSetTooltip.lines(stack(ArmorSlot.CHEST), 3);

        TranslatableContents count = findKey(lines.getFirst(), "botaniamisc.template.n_of_m");
        assertNotNull(count);
        assertArrayEquals(new Object[]{3, 4}, count.getArgs());
    }

    @Test
    void hoodTooltipListsItsAncientWills() {
        ItemStack hood = stack(ArmorSlot.HEAD);
        ((AncientWillContainer) hood.getItem()).addAncientWill(hood, AncientWillContainer.AncientWillType.DHAROK);

        assertTrue(keys(DivineSetTooltip.lines(hood, 4)).contains("botania.armorset.will_dharok.desc"));
    }

    /** Finds a translatable component by key anywhere in a component's arguments or siblings. */
    private static TranslatableContents findKey(Component c, String key) {
        if (c.getContents() instanceof TranslatableContents t) {
            if (t.getKey().equals(key)) return t;
            for (Object arg : t.getArgs()) {
                if (arg instanceof Component inner) {
                    TranslatableContents found = findKey(inner, key);
                    if (found != null) return found;
                }
            }
        }
        for (Component sibling : c.getSiblings()) {
            TranslatableContents found = findKey(sibling, key);
            if (found != null) return found;
        }
        return null;
    }

    /** A tooltip flag as the client builds it, with or without Shift held. */
    private static net.minecraft.world.item.TooltipFlag flag(boolean shift) {
        return new net.minecraft.world.item.TooltipFlag() {
            @Override
            public boolean isAdvanced() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }

            @Override
            public boolean hasShiftDown() {
                return shift;
            }
        };
    }

    private static List<Component> hover(ItemStack stack, boolean shift) {
        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, net.minecraft.world.item.Item.TooltipContext.EMPTY, lines, flag(shift));
        return lines;
    }

    @Test
    void hoverWithoutShiftShowsDivineAndTheShiftHint() {
        List<String> keys = keys(hover(stack(ArmorSlot.CHEST), false));

        assertTrue(keys.contains("tooltip.arsbotania.divine"), keys.toString());
        assertTrue(keys.contains("botaniamisc.shiftinfo"), keys.toString());
        assertFalse(keys.contains("botaniamisc.armorset"), keys.toString());
    }

    @Test
    void hoverWithShiftShowsTheSetBonuses() {
        List<Component> lines = hover(stack(ArmorSlot.CHEST), true);

        assertTrue(lines.stream().anyMatch(c -> findKey(c, "botaniamisc.armorset") != null));
        assertTrue(keys(lines).contains("botania.armorset.elementium.desc"));
    }
}
