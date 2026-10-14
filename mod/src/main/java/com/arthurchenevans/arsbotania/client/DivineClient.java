package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Tints the item icon's dye layer, the same way Ars tints its own armor icons. */
@EventBusSubscriber(modid = ArsBotania.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DivineClient {
    private DivineClient() {
    }

    @SubscribeEvent
    public static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer > 0 ? -1
                        : FastColor.ARGB32.opaque(stack.getOrDefault(DataComponents.BASE_COLOR, DyeColor.PURPLE).getTextureDiffuseColor()),
                DivineItems.all().toArray(new net.minecraft.world.item.Item[0]));
    }
}
