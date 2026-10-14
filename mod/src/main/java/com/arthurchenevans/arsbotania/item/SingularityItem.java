package com.arthurchenevans.arsbotania.item;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ShattererArea;
import com.arthurchenevans.arsbotania.core.divine.Singularity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.SequentialBreaker;
import vazkii.botania.api.item.SpecialBlockBreakingHandler;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.StoneOfTemperanceItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.relic.RingOfThorItem;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;
import java.util.function.Predicate;

/**
 * The Singularity: an S-rank Terra Shatterer and a Terra Truncator in one, mining as pickaxe,
 * axe, shovel and hoe. Divine, so no ranks to climb and no durability.
 * Botania's own breaking hook calls {@link #onBlockStartBreak}; tree felling is handed to
 * Botania's Terra Truncator so trees come down exactly as Botania cuts them.
 */
public class SingularityItem extends Item implements SpecialBlockBreakingHandler, SequentialBreaker {

    public SingularityItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC)
                .component(BotaniaDataComponents.ACTIVE, Unit.INSTANCE)
                .component(net.minecraft.core.component.DataComponents.TOOL, tool()));
    }

    /** Mines everything a netherite pickaxe, axe, shovel or hoe can; speed comes from config at use. */
    private static Tool tool() {
        float speed = 1.5F; // placeholder: getDestroySpeed below reads the configured speed
        return new Tool(List.of(
                Tool.Rule.deniesDrops(BlockTags.INCORRECT_FOR_NETHERITE_TOOL),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, speed),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, speed),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, speed),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, speed)), 1.0F, 0);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        return base > 1 ? (float) DivineConfig.settings().singularityMiningSpeed() : base;
    }

    public static boolean isAreaMode(ItemStack stack) {
        return stack.has(BotaniaDataComponents.ACTIVE);
    }

    public static void setAreaMode(ItemStack stack, boolean on) {
        if (on) stack.set(BotaniaDataComponents.ACTIVE, Unit.INSTANCE);
        else stack.remove(BotaniaDataComponents.ACTIVE);
    }

    /** Sneak + use toggles the Shatterer's area, as on Botania's pick. */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isSecondaryUseActive()) return InteractionResultHolder.pass(stack);
        setAreaMode(stack, !isAreaMode(stack));
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.TERRA_SHATTERER_MODE, SoundSource.PLAYERS, 1F, 1F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void onBlockStartBreak(ServerLevel level, ItemStack stack, BlockPos pos, Player player) {
        BlockHitResult hit = ToolCommons.raytraceFromEntity(player, 10, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        Direction face = hit.getDirection();
        breakOtherBlock(player, stack, pos, pos, face);
        if (player.isSecondaryUseActive()) BotaniaAPI.instance().breakOnAllCursors(player, stack, pos, face);
    }

    @Override
    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        boolean temperance = StoneOfTemperanceItem.hasTemperanceActive(player);
        switch (Singularity.actionFor(state.is(BotaniaTags.Blocks.TERRA_TRUNCATOR_TRUNK_BLOCKS),
                player.isShiftKeyDown(), temperance, isAreaMode(stack))) {
            case FELL_TREE -> ((SequentialBreaker) BotaniaItems.TERRA_TRUNCATOR).breakOtherBlock(player, stack, pos, originPos, side);
            case AREA -> shatter(player, stack, level, pos, side, temperance);
            case SINGLE -> { }
        }
    }

    private void shatter(Player player, ItemStack stack, Level level, BlockPos pos, Direction side, boolean temperance) {
        Predicate<BlockState> canMine = s -> (!s.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(s))
                && stack.getDestroySpeed(s) > 1;
        if (level.isEmptyBlock(pos) || !canMine.test(level.getBlockState(pos))) return;

        int rank = temperance ? ShattererArea.temperance(ShattererArea.S_RANK) : ShattererArea.S_RANK;
        boolean thor = !RingOfThorItem.getThorRing(player).isEmpty();
        ShattererArea area = ShattererArea.forFace(axisOf(side), rank, thor);
        ToolCommons.removeBlocksInIteration(player, stack, level, pos,
                new Vec3i(area.begin()[0], area.begin()[1], area.begin()[2]),
                new Vec3i(area.end()[0], area.end()[1], area.end()[2]), canMine);
    }

    private static ShattererArea.Axis axisOf(Direction side) {
        return switch (side.getAxis()) {
            case X -> ShattererArea.Axis.X;
            case Y -> ShattererArea.Axis.Y;
            case Z -> ShattererArea.Axis.Z;
        };
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.arsbotania.divine").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(isAreaMode(stack) ? "tooltip.arsbotania.singularity.area_on" : "tooltip.arsbotania.singularity.area_off")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.arsbotania.singularity.trees").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.arsbotania.singularity.toggle").withStyle(ChatFormatting.DARK_GRAY));
    }
}
