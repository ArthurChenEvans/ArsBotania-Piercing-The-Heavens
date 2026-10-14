package com.arthurchenevans.arsbotania.event;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.FlugelFlight;
import com.arthurchenevans.arsbotania.core.divine.WingStyle;
import com.arthurchenevans.arsbotania.item.DivineRobesItem;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.StoneOfTemperanceItem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Flügel Tiara flight for the Divine robes. Botania only checks for its own tiara, so this
 * mirrors the tiara's per-tick logic (both sides, like Botania) with the robes as the item.
 */
@EventBusSubscriber(modid = ArsBotania.MODID)
public final class DivineFlightEvents {
    /** Players (by UUID, per side) this mod granted flight to, so it only takes away flight it gave. */
    private static final Set<String> FLYERS = Collections.synchronizedSet(new HashSet<>());

    private DivineFlightEvents() {
    }

    private static String key(Player player) {
        return player.getUUID() + ":" + player.level().isClientSide();
    }

    public static ItemStack wornRobes(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof DivineRobesItem ? chest : ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ItemStack robes = wornRobes(player);
        updateFlyStatus(player, robes);
        if (!robes.isEmpty()) tickRobes(player, robes);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        String id = event.getEntity().getUUID().toString();
        FLYERS.remove(id + ":false");
        FLYERS.remove(id + ":true");
    }

    private static boolean shouldHaveFlight(Player player, ItemStack robes) {
        if (robes.isEmpty()) return false;
        int left = robes.getOrDefault(BotaniaDataComponents.REMAINING_TICKS, FlugelFlight.MAX_FLIGHT_TIME);
        boolean hasEye = player.getInventory().contains(new ItemStack(BotaniaItems.EYE_OF_THE_FLUGEL));
        boolean mana = ManaItemHandler.instance().requestManaExact(robes, player, FlugelFlight.cost(left), false);
        return FlugelFlight.canFly(left, robes.has(BotaniaDataComponents.FLYING), hasEye, mana);
    }

    private static void updateFlyStatus(Player player, ItemStack robes) {
        String key = key(player);
        if (FLYERS.contains(key)) {
            if (shouldHaveFlight(player, robes)) {
                player.getAbilities().mayfly = true;
                if (player.getAbilities().flying) {
                    if (!player.level().isClientSide()) {
                        if (!player.isCreative() && !player.isSpectator()) {
                            int left = robes.getOrDefault(BotaniaDataComponents.REMAINING_TICKS, FlugelFlight.MAX_FLIGHT_TIME);
                            ManaItemHandler.instance().requestManaExact(robes, player, FlugelFlight.cost(left), true);
                        }
                    } else if (Math.abs(player.getDeltaMovement().x()) > 0.1 || Math.abs(player.getDeltaMovement().z()) > 0.1) {
                        sparkle(player, DivineRobesItem.wingStyle(robes));
                    }
                }
            } else {
                if (!player.isSpectator() && !player.isCreative()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.getAbilities().invulnerable = false;
                }
                FLYERS.remove(key);
            }
        } else if (shouldHaveFlight(player, robes)) {
            FLYERS.add(key);
            player.getAbilities().mayfly = true;
        }
    }

    /** The tiara's worn tick: flight bar, sprint dash and sneak glide. */
    private static void tickRobes(Player player, ItemStack robes) {
        boolean flying = player.getAbilities().flying;
        boolean wasSprinting = robes.has(BotaniaDataComponents.IS_SPRINTING);
        boolean sprinting = player.isSprinting();
        setFlag(robes, BotaniaDataComponents.IS_SPRINTING, sprinting);

        int time = robes.getOrDefault(BotaniaDataComponents.REMAINING_TICKS, FlugelFlight.MAX_FLIGHT_TIME);
        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        boolean gliding = false;

        if (flying) {
            boolean onCooldown = player.getCooldowns().isOnCooldown(robes.getItem());
            if (!wasSprinting && sprinting && !onCooldown && !StoneOfTemperanceItem.hasTemperanceActive(player)) {
                player.setDeltaMovement(player.getDeltaMovement().add(look.x, 0, look.z));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.DASH, SoundSource.PLAYERS, 1F, 1F);
                player.getCooldowns().addCooldown(robes.getItem(), FlugelFlight.DASH_COOLDOWN);
                setFlag(robes, BotaniaDataComponents.BOOST_PENDING, true);
            } else if (onCooldown && robes.has(BotaniaDataComponents.BOOST_PENDING)) {
                player.moveRelative(5F, new Vec3(0F, 0F, 1F));
                robes.remove(BotaniaDataComponents.BOOST_PENDING);
            }
        } else {
            gliding = FlugelFlight.shouldGlide(player.isShiftKeyDown(), player.onGround(),
                    player.getDeltaMovement().y(), robes.has(BotaniaDataComponents.IS_GLIDING));
            if (gliding) {
                float mul = 0.6F;
                player.setDeltaMovement(look.x * mul, Math.max(-0.15F, player.getDeltaMovement().y()), look.z * mul);
                player.fallDistance = 2F;
            }
            setFlag(robes, BotaniaDataComponents.IS_GLIDING, gliding);
        }

        boolean creative = player.isSpectator() || player.isCreative();
        int newTime = FlugelFlight.nextRemaining(time, flying, creative, gliding, player.tickCount);
        setFlag(robes, BotaniaDataComponents.FLYING, flying);
        if (newTime != time) robes.set(BotaniaDataComponents.REMAINING_TICKS, newTime);
    }

    private static void setFlag(ItemStack stack, net.minecraft.core.component.DataComponentType<net.minecraft.util.Unit> flag, boolean on) {
        if (on) stack.set(flag, net.minecraft.util.Unit.INSTANCE);
        else stack.remove(flag);
    }

    /** Flight sparkles, coloured like the tiara's for Botania styles and rainbow for Divine. */
    private static void sparkle(Player player, WingStyle style) {
        float r = 1F, g = 1F, b = 1F;
        switch (style) {
            case ONE_WINGED_ANGEL -> { r = 0.1F; g = 0.1F; b = 0.1F; }
            case ICE_FAIRY -> { r = 0F; g = 0.6F; }
            case PHOENIX -> { g = 0.3F; b = 0.3F; }
            case BLACK_SNOW_PRINCESS -> { r = 0.6F; g = 0F; b = 0.6F; }
            case LORD_OF_THE_PIT -> { r = 0.4F; g = 0F; b = 0F; }
            case SYLPH -> { r = 0.2F; g = 0.6F; b = 0.2F; }
            case MEGA_ULTRA_CHICKEN -> { r = 0.85F; g = 0.85F; b = 0F; }
            case THE_ONE -> { r = 0F; b = 0F; }
            case DIVINE -> {
                int rgb = Mth.hsvToRgb((player.tickCount % 60) / 60F, 0.6F, 1F);
                r = ((rgb >> 16) & 0xFF) / 255F;
                g = ((rgb >> 8) & 0xFF) / 255F;
                b = (rgb & 0xFF) / 255F;
            }
            default -> { }
        }
        double x = player.getX() - 0.5, y = player.getY() - 0.5, z = player.getZ() - 0.5;
        for (int i = 0; i < 2; i++) {
            SparkleParticleData data = SparkleParticleData.sparkle(2F * (float) Math.random(), r, g, b, 20);
            player.level().addParticle(data, x + Math.random() * player.getBbWidth(), y + Math.random() * 0.4, z + Math.random() * player.getBbWidth(), 0, 0, 0);
        }
    }
}
