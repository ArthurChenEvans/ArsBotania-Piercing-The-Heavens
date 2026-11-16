package com.arthurchenevans.arsbotania.registry;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.block.DivineSourceTankBlock;
import com.arthurchenevans.arsbotania.block.DivineSourceTankTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArsBotania.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArsBotania.MODID);

    public static final DeferredBlock<Block> DIVINE_SOURCE_TANK = BLOCKS.register("divine_source_tank", DivineSourceTankBlock::new);
    public static final DeferredHolder<Item, BlockItem> DIVINE_SOURCE_TANK_ITEM = DivineItems.ITEMS.register("divine_source_tank",
            () -> new BlockItem(DIVINE_SOURCE_TANK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DivineSourceTankTile>> DIVINE_SOURCE_TANK_TILE =
            TILES.register("divine_source_tank", () -> BlockEntityType.Builder.of(DivineSourceTankTile::new, DIVINE_SOURCE_TANK.get()).build(null));

    private ModBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        TILES.register(modBus);
    }
}
