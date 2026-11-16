package com.arthurchenevans.arsbotania.registry;

import com.arthurchenevans.arsbotania.core.divine.VirtueRune;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Items made along the crafting chain to Divine armor. */
public final class ChainItems {
    private static final Map<VirtueRune, DeferredHolder<Item, Item>> VIRTUES = new EnumMap<>(VirtueRune.class);

    static {
        for (VirtueRune v : VirtueRune.values()) {
            VIRTUES.put(v, DivineItems.ITEMS.register(v.registryName(), () -> new Item(new Item.Properties().rarity(Rarity.RARE))));
        }
    }

    public static final DeferredHolder<Item, Item> RUNE_OF_LIFE_AND_DEATH = epic("rune_of_life_and_death");
    public static final DeferredHolder<Item, Item> YIN_YANG = epic("yin_yang");
    public static final DeferredHolder<Item, Item> SPACE_COMPONENT = epic("space_component");
    public static final DeferredHolder<Item, Item> TIME_COMPONENT = epic("time_component");
    public static final DeferredHolder<Item, Item> SPACETIME_FABRIC = epic("spacetime_fabric");
    public static final DeferredHolder<Item, Item> DIVINE_ICHOR = epic("divine_ichor");

    private ChainItems() {
    }

    private static DeferredHolder<Item, Item> epic(String name) {
        return DivineItems.ITEMS.register(name, () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    }

    public static Item virtue(VirtueRune rune) {
        return VIRTUES.get(rune).get();
    }

    /** In chain order, for the creative tab. */
    public static List<Item> all() {
        List<Item> items = new java.util.ArrayList<>();
        VIRTUES.values().forEach(h -> items.add(h.get()));
        for (DeferredHolder<Item, Item> h : List.of(RUNE_OF_LIFE_AND_DEATH, YIN_YANG, SPACE_COMPONENT, TIME_COMPONENT, SPACETIME_FABRIC, DIVINE_ICHOR)) {
            items.add(h.get());
        }
        return items;
    }

    /** Forces class loading so the static registrations run before the register is attached. */
    static void init() {
    }
}
