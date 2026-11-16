package com.arthurchenevans.arsbotania.entity;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.Michael;
import com.arthurchenevans.arsbotania.core.divine.VirtueRune;
import com.arthurchenevans.arsbotania.registry.ChainItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.entity.MagicMissileEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Michael, the archangel. Hovers near the beacon he was summoned at, teleports beside
 * players and fires Botania's homing magic missiles; below half health he also calls
 * down lightning. On death, everyone who hurt him in the last 30 seconds and carries all
 * seven sin runes has one of each turned into its virtue.
 */
public class MichaelEntity extends Monster {
    private static final double ARENA_RADIUS = 16;
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.arsbotania.michael"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
    private final Map<UUID, Long> lastHitBy = new HashMap<>();
    /** Fallback for fighters the server's player list can't find (fake players). */
    private final Map<UUID, WeakReference<Player>> fighters = new HashMap<>();
    private @Nullable BlockPos home;

    public MichaelEntity(EntityType<? extends MichaelEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 800)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ATTACK_DAMAGE, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** Sets up a freshly summoned Michael at a beacon, with the configured health. */
    public void summonAt(BlockPos beacon) {
        home = beacon;
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(DivineConfig.settings().michaelHealth());
        setHealth(getMaxHealth());
        moveTo(beacon.getX() + 0.5, beacon.getY() + 3, beacon.getZ() + 0.5, 0, 0);
    }

    public @Nullable BlockPos home() {
        return home;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && !level().isClientSide()) recordHit(player);
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) grantVirtues(level);
    }

    /** One of each sin rune becomes its virtue, for every recent fighter who carries all seven. */
    void grantVirtues(ServerLevel level) {
        long now = level.getGameTime();
        int window = DivineConfig.settings().michaelConversionWindowTicks();
        for (Map.Entry<UUID, Long> hit : lastHitBy.entrySet()) {
            Player player = level.getPlayerByUUID(hit.getKey());
            if (player == null) player = fighters.getOrDefault(hit.getKey(), new WeakReference<>(null)).get();
            if (player == null || !Michael.earnsVirtues(hit.getValue(), now, window, sinsCarried(player.getInventory()))) continue;
            for (VirtueRune virtue : VirtueRune.values()) {
                takeOne(player.getInventory(), sinRune(virtue));
                ItemStack reward = new ItemStack(ChainItems.virtue(virtue));
                if (!player.getInventory().add(reward)) player.drop(reward, false);
            }
        }
    }

    /** Records a player hit, as if they had just struck him (tests and commands). */
    public void recordHit(Player player) {
        lastHitBy.put(player.getUUID(), level().getGameTime());
        fighters.put(player.getUUID(), new WeakReference<>(player));
    }

    private static Item sinRune(VirtueRune virtue) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(virtue.sinRuneItem()));
    }

    private static Set<String> sinsCarried(Inventory inv) {
        Set<String> sins = new HashSet<>();
        for (VirtueRune v : VirtueRune.values()) {
            if (inv.contains(new ItemStack(sinRune(v)))) sins.add(v.sin());
        }
        return sins;
    }

    private static void takeOne(Inventory inv, Item item) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) {
                inv.getItem(i).shrink(1);
                return;
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (home == null) home = blockPosition();
        hover();

        List<Player> players = level().getEntitiesOfClass(Player.class, new AABB(home).inflate(ARENA_RADIUS),
                p -> !p.isSpectator() && !p.isCreative());
        if (players.isEmpty()) return;
        Player target = players.get(getRandom().nextInt(players.size()));
        setTarget(target);
        Michael.Phase phase = Michael.phase(getHealth(), getMaxHealth());

        if (tickCount % 80 == 0 && phase.attacks().contains(Michael.Attack.TELEPORT)) teleportBeside(target);
        if (tickCount % (phase == Michael.Phase.TWO ? 15 : 30) == 0) fireMissile();
        if (tickCount % 100 == 50 && phase.attacks().contains(Michael.Attack.LIGHTNING)) smite(target);
        if (distanceToSqr(target) < 6 && tickCount % 20 == 0) doHurtTarget(target);
    }

    /** Floats a few blocks up and never strays from the beacon. */
    private void hover() {
        if (home == null) return;
        if (position().distanceTo(Vec3.atCenterOf(home)) > ARENA_RADIUS) {
            moveTo(home.getX() + 0.5, home.getY() + 3, home.getZ() + 0.5);
        }
        double wantY = home.getY() + 3 + Math.sin(tickCount * 0.05) * 0.5;
        setDeltaMovement(getDeltaMovement().multiply(0.8, 0, 0.8).add(0, (wantY - getY()) * 0.1, 0));
    }

    private void teleportBeside(Player target) {
        double angle = getRandom().nextDouble() * Math.PI * 2;
        moveTo(target.getX() + Math.cos(angle) * 3, target.getY() + 2, target.getZ() + Math.sin(angle) * 3);
        playSound(net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 1F, 1.4F);
    }

    /** Botania's Gaia Guardian missile. */
    private void fireMissile() {
        MagicMissileEntity missile = new MagicMissileEntity(this, true);
        missile.setPos(getX(), getY() + 2.4, getZ());
        if (missile.findTarget()) {
            playSound(BotaniaSounds.MISSILE, 1F, 0.8F + getRandom().nextFloat() * 0.2F);
            level().addFreshEntity(missile);
        }
    }

    private void smite(Player target) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
        if (bolt != null) {
            bolt.moveTo(target.position());
            level().addFreshEntity(bolt);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (home != null) tag.put("home", NbtUtils.writeBlockPos(home));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NbtUtils.readBlockPos(tag, "home").ifPresent(p -> home = p);
    }
}
