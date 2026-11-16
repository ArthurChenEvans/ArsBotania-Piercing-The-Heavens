package com.arthurchenevans.arsbotania.crafting;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.RecipeCost;
import com.arthurchenevans.arsbotania.registry.ModRecipes;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** An Enchanting Apparatus recipe whose Source cost comes from config (Spacetime Fabric). */
public class ConfiguredApparatusRecipe extends EnchantingApparatusRecipe {
    private final RecipeCost cost;

    public ConfiguredApparatusRecipe(Ingredient reagent, ItemStack result, List<Ingredient> pedestals, RecipeCost cost) {
        super(reagent, result, pedestals, 0, false);
        this.cost = cost;
    }

    public RecipeCost cost() {
        return cost;
    }

    @Override
    public int sourceCost() {
        return DivineConfig.settings().cost(cost);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.CONFIGURED_APPARATUS.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.DIVINE_APPARATUS_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ConfiguredApparatusRecipe> {
        private static final MapCodec<ConfiguredApparatusRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("reagent").forGetter(EnchantingApparatusRecipe::reagent),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(EnchantingApparatusRecipe::result),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("pedestals").forGetter(EnchantingApparatusRecipe::pedestalItems),
                Codec.STRING.xmap(RecipeCost::fromKey, RecipeCost::key).fieldOf("cost").forGetter(ConfiguredApparatusRecipe::cost)
        ).apply(i, ConfiguredApparatusRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ConfiguredApparatusRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, EnchantingApparatusRecipe::reagent,
                ItemStack.STREAM_CODEC, EnchantingApparatusRecipe::result,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), r -> new ArrayList<>(r.pedestalItems()),
                ByteBufCodecs.STRING_UTF8.map(RecipeCost::fromKey, RecipeCost::key), ConfiguredApparatusRecipe::cost,
                ConfiguredApparatusRecipe::new);

        @Override
        public MapCodec<ConfiguredApparatusRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ConfiguredApparatusRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
