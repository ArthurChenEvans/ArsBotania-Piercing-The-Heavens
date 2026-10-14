package com.arthurchenevans.arsbotania.registry;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArsBotania.MODID);

    static {
        TABS.register("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.arsbotania"))
                .icon(() -> new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.ARCANIST, ArmorSlot.HEAD))))
                .displayItems((params, out) -> {
                    DivineItems.all().forEach(out::accept);
                    out.accept(DivineItems.SINGULARITY.get());
                    out.accept(DivineItems.DIVINE_BLADE.get());
                })
                .build());
    }

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
