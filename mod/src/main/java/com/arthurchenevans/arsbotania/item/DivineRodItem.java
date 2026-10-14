package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.combo.DivineCombos;
import com.arthurchenevans.arsbotania.core.divine.RodPower;
import com.arthurchenevans.arsbotania.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Divine Rod: every Botania rod's power in one rod, one selected at a time.
 * Each power is run by the Botania rod's own code with this stack, so costs and behaviour
 * are Botania's. Sneak + use in the air cycles the power.
 */
public class DivineRodItem extends Item {

    public DivineRodItem() {
        super(new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    public static RodPower power(ItemStack stack) {
        return RodPower.fromIndex(stack.getOrDefault(ModDataComponents.ROD_POWER.get(), RodPower.DEFAULT.ordinal()));
    }

    public static void setPower(ItemStack stack, RodPower power) {
        stack.set(ModDataComponents.ROD_POWER.get(), power.ordinal());
    }

    /** The Botania rod item for the selected power. */
    public static Item rod(ItemStack stack) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(power(stack).botaniaItem()));
    }

    public static Component powerName(RodPower power) {
        return Component.translatable("item.botania." + power.botaniaItem().substring("botania:".length()));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            RodPower next = power(stack).next();
            setPower(stack, next);
            if (!level.isClientSide()) player.displayClientMessage(powerName(next), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (DivineCombos.tryCombo(player, hand, null)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        return rod(stack).use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player != null && !player.isShiftKeyDown() && DivineCombos.tryCombo(player, ctx.getHand(),
                new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), ctx.isInside()))) {
            return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide());
        }
        return rod(ctx.getItemInHand()).useOn(ctx);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return rod(stack).getUseDuration(stack, entity);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return rod(stack).getUseAnimation(stack);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remaining) {
        rod(stack).onUseTick(level, entity, stack, remaining);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        rod(stack).releaseUsing(stack, level, entity, timeLeft);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        rod(stack).inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public boolean onEntitySwing(@NotNull ItemStack stack, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        return rod(stack).onEntitySwing(stack, entity, hand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsbotania.divine").withStyle(ChatFormatting.GOLD));
        tooltip.add(powerName(power(stack)).copy().withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.arsbotania.rod.cycle").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.arsbotania.combo").withStyle(ChatFormatting.DARK_GRAY));
    }
}
