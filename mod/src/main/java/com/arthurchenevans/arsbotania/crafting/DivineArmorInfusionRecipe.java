package com.arthurchenevans.arsbotania.crafting;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.DivineTier;
import com.arthurchenevans.arsbotania.core.divine.RecipeCost;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.arthurchenevans.arsbotania.registry.ModRecipes;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Divine armor infusion: a Tier 3 Ars armor piece in the apparatus centre, Divine Ichor
 * and the essences on the pedestals, the configured Source cost (50k by default). The piece
 * becomes its Divine version and keeps its threads.
 */
public class DivineArmorInfusionRecipe extends EnchantingApparatusRecipe {
    /** Ars stores Tier 3 as tier index 2. */
    private static final int ARS_TIER_THREE = 2;

    public DivineArmorInfusionRecipe(List<Ingredient> pedestals) {
        super(Ingredient.EMPTY, ItemStack.EMPTY, pedestals, 0, true);
    }

    /** The Divine piece a base Ars armor item turns into, if any. */
    public static Optional<DivinePiece> divineFor(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (DivinePiece piece : DivinePiece.all()) {
            if (piece.arsBaseItem().equals(id.toString())) return Optional.of(piece);
        }
        return Optional.empty();
    }

    @Override
    public boolean doesReagentMatch(ApparatusRecipeInput input, Level level, @Nullable Player player) {
        ArmorPerkHolder holder = PerkUtil.getPerkHolder(input.catalyst());
        return divineFor(input.catalyst()).isPresent() && holder != null && holder.getTier() == ARS_TIER_THREE;
    }

    @Override
    public @NotNull ItemStack assemble(ApparatusRecipeInput input, HolderLookup.@NotNull Provider registries) {
        ItemStack base = input.catalyst();
        ItemStack divine = new ItemStack(DivineItems.get(divineFor(base).orElseThrow()));
        ArmorPerkHolder threads = PerkUtil.getPerkHolder(base);
        if (threads != null) {
            divine.set(DataComponentRegistry.ARMOR_PERKS, threads.setTier(DivineTier.ARS_TIER_INDEX));
        }
        return divine;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(DivineItems.get(new DivinePiece(ArcanistClass.BATTLEMAGE, ArmorSlot.CHEST)));
    }

    @Override
    public int sourceCost() {
        return DivineConfig.settings().cost(RecipeCost.DIVINE_ARMOR);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.DIVINE_ARMOR_INFUSION.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.DIVINE_APPARATUS_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DivineArmorInfusionRecipe> {
        private static final MapCodec<DivineArmorInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("pedestals").forGetter(EnchantingApparatusRecipe::pedestalItems)
        ).apply(i, DivineArmorInfusionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DivineArmorInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), r -> new ArrayList<>(r.pedestalItems()),
                DivineArmorInfusionRecipe::new);

        @Override
        public MapCodec<DivineArmorInfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DivineArmorInfusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
