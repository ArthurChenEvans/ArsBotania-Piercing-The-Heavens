package com.arthurchenevans.arsbotania.event;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.DivineBlade;
import com.arthurchenevans.arsbotania.item.DivineBladeItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraBladeItem;

/**
 * The Divine Blade's Terra Blade beam: on hitting an entity, and on swinging at the air
 * (the client says so with a packet, as Botania does for its own blade).
 */
@EventBusSubscriber(modid = ArsBotania.MODID)
public final class DivineBladeEvents {
    private DivineBladeEvents() {
    }

    /** Fires a Terra Blade burst if the player swings the Divine Blade at full strength. */
    public static void trySwingBeam(Player player, float attackStrength) {
        ItemStack stack = player.getMainHandItem();
        if (player.isSpectator() || !(stack.getItem() instanceof DivineBladeItem) || !DivineBlade.firesBeam(attackStrength)) return;
        ManaBurstEntity burst = TerraBladeItem.getBurst(player, stack);
        player.level().addFreshEntity(burst);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.TERRABLADE, SoundSource.PLAYERS, 1F, 1F);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) trySwingBeam(player, player.getAttackStrengthScale(0F));
    }

    @SubscribeEvent
    public static void onSwingAtAir(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getItemStack().getItem() instanceof DivineBladeItem) {
            PacketDistributor.sendToServer(SwingPayload.INSTANCE);
        }
    }

    /** Client to server: the player swung the Divine Blade at the air. */
    public record SwingPayload() implements CustomPacketPayload {
        public static final SwingPayload INSTANCE = new SwingPayload();
        public static final Type<SwingPayload> TYPE = new Type<>(ArsBotania.prefix("divine_blade_swing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SwingPayload> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = ArsBotania.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Network {
        private Network() {
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToServer(SwingPayload.TYPE, SwingPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            trySwingBeam(context.player(), context.player().getAttackStrengthScale(0F))));
        }
    }
}
