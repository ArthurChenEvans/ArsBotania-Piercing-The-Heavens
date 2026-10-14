package com.arthurchenevans.arsbotania.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.botania.common.entity.PixieEntity;

/** Botania's pixie keeps its summoner and potion private; Divine's Elementium bonus needs both. */
@Mixin(value = PixieEntity.class, remap = false)
public interface PixieEntityAccessor {
    @Accessor("summoner")
    LivingEntity arsbotania$getSummoner();

    @Accessor("effect")
    MobEffectInstance arsbotania$getEffect();
}
