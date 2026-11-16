package com.arthurchenevans.arsbotania.block;

import com.hollingsworth.arsnouveau.common.block.SourceJar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** The Divine Source Tank: Ars's Source jar, with a much larger store. */
public class DivineSourceTankBlock extends SourceJar {

    public DivineSourceTankBlock() {
        super(BlockBehaviour.Properties.of().strength(3f, 1200f).noOcclusion(), "divine_source_tank");
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DivineSourceTankTile(pos, state);
    }
}
