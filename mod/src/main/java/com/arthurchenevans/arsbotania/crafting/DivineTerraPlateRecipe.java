package com.arthurchenevans.arsbotania.crafting;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.RecipeCost;
import com.arthurchenevans.arsbotania.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import vazkii.botania.api.recipe.ProcessingRecipeInput;
import vazkii.botania.common.crafting.TerrestrialAgglomerationRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A Terrestrial Agglomeration Plate recipe whose mana comes from config, optionally only in
 * one dimension (the space component needs the End). Out of place, the craft never starts.
 */
public class DivineTerraPlateRecipe extends TerrestrialAgglomerationRecipe {
    private final RecipeCost cost;
    private final Optional<ResourceLocation> dimension;

    public DivineTerraPlateRecipe(ItemStack output, List<Ingredient> ingredients, RecipeCost cost, Optional<ResourceLocation> dimension) {
        super(1, output, ingredients.toArray(Ingredient[]::new));
        this.cost = cost;
        this.dimension = dimension;
    }

    public RecipeCost cost() {
        return cost;
    }

    public Optional<ResourceLocation> dimension() {
        return dimension;
    }

    @Override
    public int getMana() {
        return DivineConfig.settings().cost(cost);
    }

    @Override
    public boolean matches(ProcessingRecipeInput input, Level level) {
        boolean rightPlace = dimension.map(d -> level.dimension() == ResourceKey.create(Registries.DIMENSION, d)).orElse(true);
        return rightPlace && super.matches(input, level);
    }

    @Override
    public RecipeSerializer<DivineTerraPlateRecipe> getSerializer() {
        return ModRecipes.DIVINE_TERRA_PLATE.get();
    }

    public static class Serializer implements RecipeSerializer<DivineTerraPlateRecipe> {
        private static final MapCodec<DivineTerraPlateRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(DivineTerraPlateRecipe::getOutput),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(r -> List.copyOf(r.getIngredients())),
                Codec.STRING.xmap(RecipeCost::fromKey, RecipeCost::key).fieldOf("cost").forGetter(DivineTerraPlateRecipe::cost),
                ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(DivineTerraPlateRecipe::dimension)
        ).apply(i, DivineTerraPlateRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DivineTerraPlateRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, DivineTerraPlateRecipe::getOutput,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), r -> new ArrayList<>(r.getIngredients()),
                ByteBufCodecs.STRING_UTF8.map(RecipeCost::fromKey, RecipeCost::key), DivineTerraPlateRecipe::cost,
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), DivineTerraPlateRecipe::dimension,
                DivineTerraPlateRecipe::new);

        @Override
        public MapCodec<DivineTerraPlateRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DivineTerraPlateRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
