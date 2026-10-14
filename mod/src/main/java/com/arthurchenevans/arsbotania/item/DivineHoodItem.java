package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.core.divine.AncientWill;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.item.AncientWillContainer;
import vazkii.botania.common.component.BotaniaDataComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Divine hoods take Ancient Wills like a Terrasteel helmet: Botania's own will crafting
 * recipe accepts any {@link AncientWillContainer}, and wills are stored in Botania's component.
 */
public class DivineHoodItem extends DivineArmorItem implements AncientWillContainer {

    public DivineHoodItem(DivinePiece piece, Holder<ArmorMaterial> material) {
        super(piece, material);
    }

    @Override
    public void addAncientWill(ItemStack stack, AncientWillType will) {
        Set<String> wills = new TreeSet<>(storedWills(stack));
        wills.add(will.name().toLowerCase(Locale.ROOT));
        stack.set(BotaniaDataComponents.ANCIENT_WILLS, new ArrayList<>(wills));
    }

    @Override
    public boolean hasAncientWill(ItemStack stack, AncientWillType will) {
        return storedWills(stack).contains(will.name().toLowerCase(Locale.ROOT));
    }

    public static Set<AncientWill> wills(ItemStack stack) {
        return AncientWill.parse(storedWills(stack));
    }

    private static List<String> storedWills(ItemStack stack) {
        return stack.getOrDefault(BotaniaDataComponents.ANCIENT_WILLS, Collections.emptyList());
    }
}
