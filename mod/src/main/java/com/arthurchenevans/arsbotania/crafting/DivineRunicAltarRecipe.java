package com.arthurchenevans.arsbotania.crafting;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.RecipeCost;
import com.arthurchenevans.arsbotania.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import vazkii.botania.api.recipe.ProcessingRecipeInput;
import vazkii.botania.common.crafting.RunicAltarRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * A Runic Altar recipe whose mana comes from config. With {@code time_ritual} it is the time
 * component: Overworld only, and {@code TimeRitualTracker} runs it from sunset to sunrise.
 * Ingredients listed are consumed; only listed catalysts come back.
 */
public class DivineRunicAltarRecipe extends RunicAltarRecipe {
    private final RecipeCost cost;
    private final boolean timeRitual;

    public DivineRunicAltarRecipe(ItemStack output, Ingredient reagent, List<Ingredient> ingredients, List<Ingredient> catalysts, RecipeCost cost, boolean timeRitual) {
        super(output, reagent, 1, ingredients.toArray(Ingredient[]::new), catalysts.toArray(Ingredient[]::new));
        this.cost = cost;
        this.timeRitual = timeRitual;
    }

    public RecipeCost cost() {
        return cost;
    }

    public boolean isTimeRitual() {
        return timeRitual;
    }

    @Override
    public int getMana() {
        return DivineConfig.settings().cost(cost);
    }

    @Override
    public boolean matches(ProcessingRecipeInput input, Level level) {
        return (!timeRitual || level.dimension() == Level.OVERWORLD) && super.matches(input, level);
    }

    @Override
    public RecipeSerializer<DivineRunicAltarRecipe> getSerializer() {
        return ModRecipes.DIVINE_RUNIC_ALTAR.get();
    }

    public static class Serializer implements RecipeSerializer<DivineRunicAltarRecipe> {
        private static final MapCodec<DivineRunicAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(DivineRunicAltarRecipe::getOutput),
                Ingredient.CODEC_NONEMPTY.fieldOf("reagent").forGetter(DivineRunicAltarRecipe::getReagent),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(r -> List.copyOf(r.getIngredients())),
                Ingredient.CODEC_NONEMPTY.listOf().optionalFieldOf("catalysts", List.of()).forGetter(r -> List.copyOf(r.getCatalysts())),
                Codec.STRING.xmap(RecipeCost::fromKey, RecipeCost::key).fieldOf("cost").forGetter(DivineRunicAltarRecipe::cost),
                Codec.BOOL.optionalFieldOf("time_ritual", false).forGetter(DivineRunicAltarRecipe::isTimeRitual)
        ).apply(i, DivineRunicAltarRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DivineRunicAltarRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, DivineRunicAltarRecipe::getOutput,
                Ingredient.CONTENTS_STREAM_CODEC, DivineRunicAltarRecipe::getReagent,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), r -> new ArrayList<>(r.getIngredients()),
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), r -> new ArrayList<>(r.getCatalysts()),
                ByteBufCodecs.STRING_UTF8.map(RecipeCost::fromKey, RecipeCost::key), DivineRunicAltarRecipe::cost,
                ByteBufCodecs.BOOL, DivineRunicAltarRecipe::isTimeRitual,
                DivineRunicAltarRecipe::new);

        @Override
        public MapCodec<DivineRunicAltarRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DivineRunicAltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
