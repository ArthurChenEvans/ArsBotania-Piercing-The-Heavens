package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineWingLayout;
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
    private static final String[] SWEEP = System.getProperty("arsbotania.wingSweep") == null
            ? null : System.getProperty("arsbotania.wingSweep").split(";");
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
        if (SWEEP != null) {
            sweep(mc);
            return;
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

    /**
     * {@code -Darsbotania.wingSweep=scale:height:tilt;...} photographs each wing layout from
     * the back and the front, then quits.
     */
    private static void sweep(Minecraft mc) {
        int stage = (ticks - 200) / 30;
        int step = (ticks - 200) % 30;
        if (ticks < 200) return;
        if (stage >= SWEEP.length * 2) {
            mc.stop();
            return;
        }
        String[] p = SWEEP[stage % SWEEP.length].split(":");
        if (step == 0) {
            DivineWingsLayer.layoutOverride = new DivineWingLayout.Pair(Float.parseFloat(p[0]), Float.parseFloat(p[1]), Float.parseFloat(p[2]));
            mc.options.setCameraType(stage < SWEEP.length ? CameraType.THIRD_PERSON_BACK : CameraType.THIRD_PERSON_FRONT);
        }
        if (step == 20) {
            shot(mc, (stage < SWEEP.length ? "back" : "front") + "_s" + p[0] + "_h" + p[1] + "_t" + p[2]);
        }
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "divine_" + PROPERTY + "_" + name + ".png", mc.getMainRenderTarget(), msg -> {
        });
    }
}
