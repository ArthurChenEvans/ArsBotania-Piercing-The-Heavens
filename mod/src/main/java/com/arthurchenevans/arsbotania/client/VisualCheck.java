package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;

/**
 * Dev-only: with {@code -Darsbotania.visualCheck=<class>} the client dresses the player in
 * that class's Divine set and saves screenshots (back, front, flying) to {@code screenshots/},
 * then quits. Lets wing and armor rendering be checked without a person at the keyboard.
 * Does nothing unless the property is set.
 */
@EventBusSubscriber(modid = ArsBotania.MODID, value = Dist.CLIENT)
public final class VisualCheck {
    private static final String PROPERTY = System.getProperty("arsbotania.visualCheck");
    private static int ticks = -1;

    private VisualCheck() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (PROPERTY == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        ticks++;

        if (ticks == 1) {
            ArcanistClass cls = ArcanistClass.valueOf(PROPERTY.toUpperCase());
            mc.getSingleplayerServer().execute(() -> {
                ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if (player == null) return;
                player.setGameMode(GameType.SURVIVAL);
                for (ArmorSlot slot : ArmorSlot.values()) {
                    player.setItemSlot(DivineItems.toEquipmentSlot(slot), new ItemStack(DivineItems.get(new DivinePiece(cls, slot))));
                }
                ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
                tablet.set(BotaniaDataComponents.MANA, 500_000);
                player.getInventory().setItem(8, tablet);
                player.serverLevel().setDayTime(6000);
                player.setYRot(0);
                player.setXRot(10);
            });
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if (ticks == 200) shot(mc, "back");
        if (ticks == 210) mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        if (ticks == 260) shot(mc, "front");
        if (ticks == 270) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            mc.player.getAbilities().flying = true;
            mc.player.onUpdateAbilities();
        }
        if (ticks == 330) shot(mc, "flying");
        if (ticks == 340) mc.stop();
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "divine_" + PROPERTY + "_" + name + ".png", mc.getMainRenderTarget(), msg -> {
        });
    }
}
