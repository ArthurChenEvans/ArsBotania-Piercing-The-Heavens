package com.arthurchenevans.arsbotania.mixin;

import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;

/** The time ritual reads the altar's current recipe and caps its mana. */
@Mixin(value = RunicAltarBlockEntity.class, remap = false)
public interface RunicAltarAccessor {
    @Accessor("currentRecipe")
    RecipeHolder<RunicAltarRecipe> arsbotania$getCurrentRecipe();

    @Accessor("mana")
    int arsbotania$getMana();

    @Accessor("mana")
    void arsbotania$setMana(int mana);
}
