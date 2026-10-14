package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.WingStyle;
import com.arthurchenevans.arsbotania.event.DivineFlightEvents;
import com.arthurchenevans.arsbotania.item.DivineRobesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;

/** Client wiring for the Divine robes' wings and flight bar. */
@EventBusSubscriber(modid = ArsBotania.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DivineClient {
    private DivineClient() {
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(DivineWingsLayer.DIVINE_WINGS);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new DivineWingsLayer(renderer));
            }
        }
    }

    /** The tiara's flight bar, drawn by Botania's own HUD code, above the air bubbles. */
    @SubscribeEvent
    public static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, ArsBotania.prefix("divine_flight_bar"), (gui, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui || mc.player == null || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return;
            ItemStack robes = DivineFlightEvents.wornRobes(mc.player);
            if (robes.isEmpty()) return;
            // Botania picks the bar icon from the tiara variant; Divine and no-wings use the Flügel icon.
            ItemStack shown = robes.copy();
            WingStyle style = DivineRobesItem.wingStyle(robes);
            FlugelTiaraItem.setVariant(shown, style.isBotaniaWings() ? style.botaniaVariant() : WingStyle.FLUGEL.botaniaVariant());
            FlugelTiaraItem.ClientLogic.renderHUD(gui, delta.getGameTimeDeltaPartialTick(true), mc.player, shown);
        });
    }
}
