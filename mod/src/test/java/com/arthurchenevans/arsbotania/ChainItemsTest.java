package com.arthurchenevans.arsbotania;

import com.arthurchenevans.arsbotania.core.divine.VirtueRune;
import com.arthurchenevans.arsbotania.registry.ChainItems;
import com.arthurchenevans.arsbotania.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChainItemsTest {

    @Test
    void everyChainItemIsRegisteredWithArt() {
        for (var item : ChainItems.all()) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/textures/item/" + path + ".png"), path);
            assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/models/item/" + path + ".json"), path);
        }
        assertEquals(VirtueRune.values().length + 6, ChainItems.all().size());
    }

    @Test
    void divineSourceTankIsABlockWithAnItem() {
        assertNotNull(ModBlocks.DIVINE_SOURCE_TANK.get());
        assertNotNull(ModBlocks.DIVINE_SOURCE_TANK_ITEM.get());
        assertNotNull(getClass().getResourceAsStream("/assets/arsbotania/blockstates/divine_source_tank.json"));
        assertNotNull(getClass().getResourceAsStream("/data/arsbotania/loot_table/blocks/divine_source_tank.json"));
    }
}
