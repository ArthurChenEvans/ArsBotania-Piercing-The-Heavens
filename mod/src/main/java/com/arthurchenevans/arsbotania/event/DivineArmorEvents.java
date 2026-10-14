package com.arthurchenevans.arsbotania.event;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.AncientWill;
import com.arthurchenevans.arsbotania.core.divine.BotaniaSetBonuses;
import com.arthurchenevans.arsbotania.core.divine.Bonuses;
import com.arthurchenevans.arsbotania.core.divine.CritOutcome;
import com.arthurchenevans.arsbotania.core.divine.DivineBlade;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivineBonuses;
import com.arthurchenevans.arsbotania.core.divine.DivineLoadout;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.HealthRegen;
import com.arthurchenevans.arsbotania.item.DivineBladeItem;
import com.arthurchenevans.arsbotania.item.DivineHoodItem;
import com.arthurchenevans.arsbotania.mixin.PixieEntityAccessor;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.PixieEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/** Divine armor effects that need the game: regen, mana, Ancient Wills and pixies. */
@EventBusSubscriber(modid = ArsBotania.MODID)
public final class DivineArmorEvents {
    /** Botania's Elementium pixie potions. */
    private static final List<Supplier<MobEffectInstance>> PIXIE_POTIONS = List.of(
            () -> new MobEffectInstance(MobEffects.BLINDNESS, 40, 0),
            () -> new MobEffectInstance(MobEffects.WITHER, 50, 0),
            () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0),
            () -> new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));

    /** The target of a critical hit, held until the hit lands a moment later in the same attack. */
    private static final Map<Player, LivingEntity> CRIT_TARGETS = new WeakHashMap<>();

    private DivineArmorEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        DivineLoadout loadout = DivineItems.loadoutOf(player);
        if (loadout.count() == 0) return;

        DivineArmorSettings settings = DivineConfig.settings();
        Bonuses bonuses = DivineBonuses.of(loadout, settings);
        BotaniaSetBonuses setBonuses = new BotaniaSetBonuses(settings);
        // Botania ignores dispatches from an empty stack, so a worn Divine piece is the source.
        ItemStack source = wornDivinePiece(player, loadout);

        if (bonuses.botaniaManaPerTick() > 0) {
            ManaItemHandler.instance().dispatchMana(source, player, bonuses.botaniaManaPerTick(), true);
        }
        int tabletMana = setBonuses.tabletManaAt(player.tickCount, loadout);
        if (tabletMana > 0) {
            ManaItemHandler.instance().dispatchManaExact(source, player, tabletMana, true);
        }

        float heal = new HealthRegen(settings).healAt(player.level().getGameTime(), bonuses, player.getHealth(), player.getMaxHealth());
        heal += setBonuses.hungerRegenAt(player.tickCount, player.getFoodData().getFoodLevel(),
                player.getHealth() + heal, player.getMaxHealth(), loadout);
        if (heal > 0) player.heal(heal);
    }

    /** Terrasteel's Ancient Wills, on the Divine hood. Runs after Botania's own crit handler. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !event.isCriticalHit() || !(event.getTarget() instanceof LivingEntity target)) return;

        Set<AncientWill> wills = hoodWills(player);
        CritOutcome outcome = AncientWill.onCrit(wills, DivineItems.loadoutOf(player).isFullSet(),
                player.getHealth(), player.getMaxHealth(), 0f);
        if (outcome == CritOutcome.NONE) return;

        event.setDamageMultiplier(event.getDamageMultiplier() * outcome.damageMultiplier());
        CRIT_TARGETS.put(player, target);
    }

    @SubscribeEvent
    public static void onCritLands(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        LivingEntity target = event.getEntity();
        if (CRIT_TARGETS.get(player) != target) return;
        CRIT_TARGETS.remove(player);

        CritOutcome outcome = AncientWill.onCrit(hoodWills(player), DivineItems.loadoutOf(player).isFullSet(),
                player.getHealth(), player.getMaxHealth(), event.getAmount());
        for (CritOutcome.Effect effect : outcome.effects()) {
            target.addEffect(new MobEffectInstance(toMobEffect(effect.type()), effect.durationTicks(), effect.amplifier()));
        }
        if (outcome.heal() > 0) player.heal(outcome.heal());
        if (outcome.armorPiercing()) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> 0f);
        }
    }

    /** Elementium set bonus: Botania only gives pixies a potion for its own set, so Divine adds it here. */
    @SubscribeEvent
    public static void onPixieSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof PixieEntity pixie) || pixie.isGaia()) return;
        PixieEntityAccessor access = (PixieEntityAccessor) pixie;
        if (!(access.arsbotania$getSummoner() instanceof Player player)) return;

        // Elementium Sword bonus: Botania only checks for its own sword, so the Divine Blade adds it here.
        if (player.getMainHandItem().getItem() instanceof DivineBladeItem) {
            access.arsbotania$setDamage(DivineBlade.pixieDamage(true));
        }
        if (access.arsbotania$getEffect() != null) return;

        if (new BotaniaSetBonuses(DivineConfig.settings()).pixiesApplyPotions(DivineItems.loadoutOf(player))) {
            pixie.setApplyPotionEffect(PIXIE_POTIONS.get(player.getRandom().nextInt(PIXIE_POTIONS.size())).get());
        }
    }

    private static Set<AncientWill> hoodWills(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return head.getItem() instanceof DivineHoodItem ? DivineHoodItem.wills(head) : Set.of();
    }

    private static Holder<MobEffect> toMobEffect(CritOutcome.EffectType type) {
        return switch (type) {
            case WEAKNESS -> MobEffects.WEAKNESS;
            case SLOWNESS -> MobEffects.MOVEMENT_SLOWDOWN;
            case WITHER -> MobEffects.WITHER;
        };
    }

    private static ItemStack wornDivinePiece(Player player, DivineLoadout loadout) {
        DivinePiece piece = loadout.pieces().iterator().next();
        return player.getItemBySlot(DivineItems.toEquipmentSlot(piece.slot()));
    }
}
