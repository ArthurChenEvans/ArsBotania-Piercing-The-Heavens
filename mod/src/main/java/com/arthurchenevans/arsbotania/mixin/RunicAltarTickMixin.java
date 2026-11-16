package com.arthurchenevans.arsbotania.mixin;

import com.arthurchenevans.arsbotania.ritual.TimeRitualTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;

/** Runs the time component's overnight ritual on every server tick of a Runic Altar. */
@Mixin(value = RunicAltarBlockEntity.class, remap = false)
public abstract class RunicAltarTickMixin {
    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void arsbotania$timeRitual(Level level, BlockPos pos, BlockState state, RunicAltarBlockEntity self, CallbackInfo ci) {
        TimeRitualTracker.tick(level, pos, self);
    }
}
