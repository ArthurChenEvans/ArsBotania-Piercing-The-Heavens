package com.arthurchenevans.arsbotania.combo;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.ComboOrder;
import com.arthurchenevans.arsbotania.item.DivineGrimoireItem;
import com.arthurchenevans.arsbotania.item.DivineRodItem;
import com.hollingsworth.arsnouveau.api.event.SpellResolveEvent;
import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractCaster;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Divine Grimoire + Divine Rod combos, left hand first:
 * book left / rod right casts the spell, then the rod acts where it landed;
 * rod left / book right uses the rod, then the spell lands where the rod hit.
 * Both halves are paid in full by their own mod's mana.
 */
@EventBusSubscriber(modid = ArsBotania.MODID)
public final class DivineCombos {
    /** Spell-then-rod combos waiting for their spell to land. */
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();
    /** The steps each player's last combo took, in order (for tests and debugging). */
    private static final Map<UUID, List<ComboOrder.Step>> LAST = new ConcurrentHashMap<>();
    private static final int PENDING_TICKS = 200;

    private record Pending(InteractionHand rodHand, long expiresAt) {
    }

    private DivineCombos() {
    }

    public static List<ComboOrder.Step> lastSteps(Player player) {
        return LAST.getOrDefault(player.getUUID(), List.of());
    }

    private static ComboOrder.Held held(ItemStack stack) {
        if (stack.getItem() instanceof DivineGrimoireItem) return ComboOrder.Held.GRIMOIRE;
        if (stack.getItem() instanceof DivineRodItem) return ComboOrder.Held.ROD;
        return ComboOrder.Held.OTHER;
    }

    public static ComboOrder orderFor(Player player) {
        return ComboOrder.of(player.getMainArm() == HumanoidArm.RIGHT,
                held(player.getMainHandItem()), held(player.getOffhandItem()));
    }

    /**
     * Runs a combo if the player holds the grimoire and rod in opposite hands.
     * Returns true when a combo was triggered (both sides), so the item's own use is skipped.
     */
    public static boolean tryCombo(Player player, InteractionHand usedHand, @Nullable BlockHitResult clicked) {
        ComboOrder order = orderFor(player);
        if (order == ComboOrder.NONE) return false;
        if (!(player.level() instanceof ServerLevel level)) return true;

        InteractionHand bookHand = held(player.getMainHandItem()) == ComboOrder.Held.GRIMOIRE ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        InteractionHand rodHand = bookHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        BlockHitResult target = clicked != null ? clicked : lookedAtBlock(player);
        LAST.put(player.getUUID(), new ArrayList<>());

        if (order == ComboOrder.ROD_THEN_SPELL) {
            useRod(player, rodHand, target);
            castAt(player, level, bookHand, target);
        } else {
            PENDING.put(player.getUUID(), new Pending(rodHand, level.getGameTime() + PENDING_TICKS));
            castNormally(player, level, bookHand);
        }
        return true;
    }

    /** Spell then rod: when the grimoire's spell lands, the rod acts there. */
    @SubscribeEvent
    public static void onSpellLands(SpellResolveEvent.Post event) {
        if (!(event.shooter instanceof Player player) || player.level().isClientSide()) return;
        if (!(event.context.getCasterTool().getItem() instanceof DivineGrimoireItem)) return;
        Pending pending = PENDING.remove(player.getUUID());
        if (pending == null || player.level().getGameTime() > pending.expiresAt()) return;
        HitResult hit = event.rayTraceResult;
        useRod(player, pending.rodHand(), hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK ? b : null);
    }

    private static void record(Player player, ComboOrder.Step step) {
        LAST.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(step);
    }

    /** The selected rod power, at a block if there is one, otherwise used in the air. */
    private static void useRod(Player player, InteractionHand hand, @Nullable BlockHitResult at) {
        ItemStack rod = player.getItemInHand(hand);
        if (!(rod.getItem() instanceof DivineRodItem)) return;
        record(player, ComboOrder.Step.ROD);
        if (at != null && DivineRodItem.rod(rod).useOn(new UseOnContext(player, hand, at)).consumesAction()) return;
        DivineRodItem.rod(rod).use(player.level(), player, hand);
    }

    /** The grimoire's selected spell resolving straight onto the block the rod hit (mana checked and spent). */
    private static void castAt(Player player, ServerLevel level, InteractionHand bookHand, @Nullable BlockHitResult at) {
        if (at == null) {
            castNormally(player, level, bookHand);
            return;
        }
        ItemStack book = player.getItemInHand(bookHand);
        AbstractCaster<?> caster = SpellCasterRegistry.from(book);
        if (caster == null) return;
        Spell spell = caster.modifySpellBeforeCasting(level, player, bookHand, caster.getSpell());
        if (!spell.isValid()) return;
        record(player, ComboOrder.Step.SPELL);
        SpellResolver resolver = new SpellResolver(new SpellContext(level, spell, player, new PlayerCaster(player), book));
        if (resolver.canCast(player) && !resolver.postEvent().isCanceled()) {
            resolver.onResolveEffect(level, at);
            resolver.expendMana();
        }
    }

    private static void castNormally(Player player, ServerLevel level, InteractionHand bookHand) {
        AbstractCaster<?> caster = SpellCasterRegistry.from(player.getItemInHand(bookHand));
        if (caster == null) return;
        record(player, ComboOrder.Step.SPELL);
        caster.castSpell(level, player, bookHand, Component.translatable("ars_nouveau.invalid_spell"));
    }

    private static @Nullable BlockHitResult lookedAtBlock(Player player) {
        double reach = player.blockInteractionRange();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(reach));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }
}
