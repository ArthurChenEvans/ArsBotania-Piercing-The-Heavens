package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivineBonuses;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.DivineTier;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.api.perk.PerkInstance;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.client.renderer.tile.GenericModel;
import com.hollingsworth.arsnouveau.common.armor.AnimatedMagicArmor;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaDiscountArmor;

import java.util.List;

/**
 * A Divine armor piece. Built on Ars Nouveau's armor so threads, dyeing and the
 * GeckoLib model all behave like the Ars piece it comes from; stats come from config.
 */
public class DivineArmorItem extends AnimatedMagicArmor implements ManaDiscountArmor {
    private final DivinePiece piece;

    public DivineArmorItem(DivinePiece piece, Holder<ArmorMaterial> material) {
        super(material, toArmorType(piece.slot()),
                ItemsRegistry.defaultItemProperties()
                        .stacksTo(1)
                        .fireResistant()
                        .rarity(Rarity.EPIC)
                        .component(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder())
                        .component(DataComponents.BASE_COLOR, DyeColor.PURPLE),
                arsModel(piece.arcanistClass()));
        this.piece = piece;
    }

    public DivinePiece piece() {
        return piece;
    }

    private static ArmorItem.Type toArmorType(ArmorSlot slot) {
        return switch (slot) {
            case HEAD -> ArmorItem.Type.HELMET;
            case CHEST -> ArmorItem.Type.CHESTPLATE;
            case LEGS -> ArmorItem.Type.LEGGINGS;
            case FEET -> ArmorItem.Type.BOOTS;
        };
    }

    /** Same GeckoLib model Ars uses for the base class. */
    private static GenericModel<AnimatedMagicArmor> arsModel(ArcanistClass cls) {
        String name = switch (cls) {
            case SORCERER -> "light_armor";
            case ARCANIST -> "medium_armor";
            case BATTLEMAGE -> "heavy_armor";
        };
        return new GenericModel<AnimatedMagicArmor>(name, "item/" + name).withEmptyAnim();
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void verifyComponentsAfterLoad(@NotNull ItemStack stack) {
        ArmorPerkHolder holder = PerkUtil.getPerkHolder(stack);
        if (holder != null && holder.getTier() != DivineTier.ARS_TIER_INDEX) {
            stack.set(DataComponentRegistry.ARMOR_PERKS, holder.setTier(DivineTier.normaliseArsTier(holder.getTier())));
        }
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        DivineArmorSettings s = DivineConfig.settings();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(getEquipmentSlot());
        ResourceLocation id = ArsBotania.prefix("divine_armor." + piece.slot().suffix());

        ItemAttributeModifiers mods = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, s.defence(piece.slot()), AttributeModifier.Operation.ADD_VALUE), group)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, s.toughnessPerPiece(), AttributeModifier.Operation.ADD_VALUE), group)
                .add(PerkAttributes.MAX_MANA, new AttributeModifier(id, s.arsMaxManaPerPiece(), AttributeModifier.Operation.ADD_VALUE), group)
                .add(PerkAttributes.MANA_REGEN_BONUS, new AttributeModifier(id, s.arsManaRegenPerPiece(), AttributeModifier.Operation.ADD_VALUE), group)
                .build();

        ArmorPerkHolder holder = PerkUtil.getPerkHolder(stack);
        if (holder != null) {
            for (PerkInstance instance : holder.getPerkInstances(stack)) {
                mods = instance.getPerk().applyAttributeModifiers(mods, stack, instance.getSlot().value(), group);
            }
        }
        return mods;
    }

    @Override
    public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
        return (float) DivineBonuses.discountReportedBy(piece.slot(), DivineItems.loadoutOf(player), DivineConfig.settings());
    }

    /** Ars prints "Tier N" here; Divine has no tier, so only the threads are listed. */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsbotania.divine").withStyle(ChatFormatting.GOLD));
        ArmorPerkHolder holder = PerkUtil.getPerkHolder(stack);
        if (holder != null) holder.appendPerkTooltip(tooltip, stack);
    }
}
