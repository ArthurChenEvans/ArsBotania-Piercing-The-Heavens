package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.WingStyle;
import com.arthurchenevans.arsbotania.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Divine robes carry Botania's Flügel Tiara: its flight, and wings in a style of the
 * wearer's choosing. Sneak-use cycles the style instead of equipping.
 */
public class DivineRobesItem extends DivineArmorItem {

    public DivineRobesItem(DivinePiece piece, Holder<ArmorMaterial> material) {
        super(piece, material);
    }

    public static WingStyle wingStyle(ItemStack stack) {
        return WingStyle.fromId(stack.getOrDefault(ModDataComponents.WING_STYLE.get(), WingStyle.DEFAULT.id()));
    }

    public static void setWingStyle(ItemStack stack, WingStyle style) {
        stack.set(ModDataComponents.WING_STYLE.get(), style.id());
    }

    public static Component wingName(WingStyle style) {
        return style == WingStyle.DIVINE
                ? Component.translatable("arsbotania.wings.divine")
                : Component.translatable("botania.wings" + style.botaniaVariant());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);

        WingStyle next = wingStyle(stack).next();
        setWingStyle(stack, next);
        if (!level.isClientSide()) player.displayClientMessage(wingName(next), true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(wingName(wingStyle(stack)).copy().withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.arsbotania.cycle_wings").withStyle(ChatFormatting.DARK_GRAY));
    }
}
