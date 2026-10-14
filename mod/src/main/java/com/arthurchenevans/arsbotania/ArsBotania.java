package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.arthurchenevans.arsbotania.registry.ModCreativeTabs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ArsBotania.MODID)
public class ArsBotania {
    public static final String MODID = "arsbotania";

    public ArsBotania(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, DivineConfig.SPEC);
        DivineItems.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(DivineConfig::onLoad);
        modBus.addListener(DivineConfig::onReload);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DivineItems::registerThreadSlots);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
