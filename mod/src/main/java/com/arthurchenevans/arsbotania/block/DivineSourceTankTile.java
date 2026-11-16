package com.arthurchenevans.arsbotania.block;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.registry.ModBlocks;
import com.hollingsworth.arsnouveau.common.capability.SourceStorage;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * An Ars Source jar that holds far more (1M Source by default), so the Divine armor infusion
 * can draw its whole cost from one block. Being a {@link SourceJarTile}, the Enchanting
 * Apparatus, relays and jars all treat it like any other jar.
 */
public class DivineSourceTankTile extends SourceJarTile {

    public DivineSourceTankTile(BlockPos pos, BlockState state) {
        super(ModBlocks.DIVINE_SOURCE_TANK_TILE.get(), pos, state);
    }

    @Override
    protected @NotNull SourceStorage createDefaultStorage() {
        int capacity = DivineConfig.settings().divineTankCapacity();
        return new SourceStorage(capacity, capacity) {
            @Override
            public void onContentsChanged() {
                DivineSourceTankTile.this.updateBlock();
            }
        };
    }
}
