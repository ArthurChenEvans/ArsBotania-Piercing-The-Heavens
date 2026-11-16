package com.arthurchenevans.arsbotania.registry;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.crafting.ConfiguredApparatusRecipe;
import com.arthurchenevans.arsbotania.crafting.DivineArmorInfusionRecipe;
import com.arthurchenevans.arsbotania.crafting.DivineRunicAltarRecipe;
import com.arthurchenevans.arsbotania.crafting.DivineTerraPlateRecipe;
import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Recipe types and serializers for the crafting chain. */
public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ArsBotania.MODID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ArsBotania.MODID);

    /** Our Enchanting Apparatus recipes; added to Ars's list of apparatus recipe types. */
    public static final DeferredHolder<RecipeType<?>, RecipeType<EnchantingApparatusRecipe>> DIVINE_APPARATUS_TYPE =
            TYPES.register("divine_apparatus", () -> RecipeType.simple(ArsBotania.prefix("divine_apparatus")));

    public static final DeferredHolder<RecipeSerializer<?>, DivineRunicAltarRecipe.Serializer> DIVINE_RUNIC_ALTAR =
            SERIALIZERS.register("runic_altar", DivineRunicAltarRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, DivineTerraPlateRecipe.Serializer> DIVINE_TERRA_PLATE =
            SERIALIZERS.register("terra_plate", DivineTerraPlateRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, ConfiguredApparatusRecipe.Serializer> CONFIGURED_APPARATUS =
            SERIALIZERS.register("apparatus", ConfiguredApparatusRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, DivineArmorInfusionRecipe.Serializer> DIVINE_ARMOR_INFUSION =
            SERIALIZERS.register("divine_armor_infusion", DivineArmorInfusionRecipe.Serializer::new);

    private ModRecipes() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
        TYPES.register(modBus);
    }

    /** Lets the Enchanting Apparatus find our recipes. */
    public static void registerWithArs() {
        ArsNouveauAPI.getInstance().getEnchantingRecipeTypes().add(DIVINE_APPARATUS_TYPE.get());
    }
}
