package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.core.divine.AncientWill;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The shift tooltip for Divine armor, laid out like Botania's armor set tooltip and
 * reusing Botania's own lines for the Terrasteel, Manaweave and Elementium bonuses.
 */
public final class DivineSetTooltip {
    private DivineSetTooltip() {
    }

    public static List<Component> lines(ItemStack stack, int piecesWorn) {
        List<Component> lines = new ArrayList<>();
        Component count = Component.translatable("botaniamisc.template.n_of_m", piecesWorn, ArmorSlot.values().length);
        Component setInfo = Component.translatable("botaniamisc.template.parenthesis_suffix",
                Component.translatable("arsbotania.armorset.divine.name"), count).withStyle(ChatFormatting.GRAY);
        lines.add(Component.translatable("botaniamisc.armorset", setInfo));

        for (String key : List.of(
                "botania.armorset.terrasteel.desc0",
                "botania.armorset.terrasteel.desc1",
                "botania.armorset.terrasteel.desc2",
                "botania.armorset.manaweave.desc0",
                "botania.armorset.manaweave.desc1",
                "botania.armorset.elementium.desc")) {
            lines.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }

        if (stack.getItem() instanceof DivineHoodItem) {
            for (AncientWill will : DivineHoodItem.wills(stack)) {
                lines.add(Component.translatable("botania.armorset.will_" + will.id() + ".desc").withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }
}
