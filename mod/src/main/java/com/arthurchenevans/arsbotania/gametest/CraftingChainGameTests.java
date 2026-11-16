package com.arthurchenevans.arsbotania.gametest;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.block.DivineSourceTankTile;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.RecipeCost;
import com.arthurchenevans.arsbotania.core.divine.TimeRitual;
import com.arthurchenevans.arsbotania.core.divine.VirtueRune;
import com.arthurchenevans.arsbotania.crafting.DivineArmorInfusionRecipe;
import com.arthurchenevans.arsbotania.item.DivineArmorItem;
import com.arthurchenevans.arsbotania.mixin.RunicAltarAccessor;
import com.arthurchenevans.arsbotania.registry.ChainItems;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.arthurchenevans.arsbotania.registry.ModBlocks;
import com.arthurchenevans.arsbotania.ritual.TimeRitualTracker;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.block.tile.ArcanePedestalTile;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import com.hollingsworth.arsnouveau.common.crafting.recipes.IEnchantingRecipe;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.recipe.StacksProcessingRecipeInput;
import vazkii.botania.common.item.BotaniaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The crafting chain to Divine armor, step by step, against the real Botania and Ars blocks and recipes. */
@GameTestHolder(ArsBotania.MODID)
@PrefixGameTestTemplate(false)
public class CraftingChainGameTests {
    private static final String ROOM = "empty9x9";
    private static final Item[] SIN_RUNES = {BotaniaItems.RUNE_OF_LUST, BotaniaItems.RUNE_OF_GLUTTONY, BotaniaItems.RUNE_OF_GREED,
            BotaniaItems.RUNE_OF_SLOTH, BotaniaItems.RUNE_OF_WRATH, BotaniaItems.RUNE_OF_ENVY, BotaniaItems.RUNE_OF_PRIDE};
    private static final Item[] ESSENCES = {ItemsRegistry.ABJURATION_ESSENCE.get(), ItemsRegistry.CONJURATION_ESSENCE.get(),
            ItemsRegistry.AIR_ESSENCE.get(), ItemsRegistry.EARTH_ESSENCE.get(), ItemsRegistry.FIRE_ESSENCE.get(),
            ItemsRegistry.MANIPULATION_ESSENCE.get(), ItemsRegistry.WATER_ESSENCE.get()};

    private static StacksProcessingRecipeInput input(Item... items) {
        ItemStack[] stacks = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) stacks[i] = new ItemStack(items[i]);
        return new StacksProcessingRecipeInput(stacks);
    }

    private static Optional<RecipeHolder<RunicAltarRecipe>> altar(Level level, Item... items) {
        return level.getRecipeManager().getRecipeFor(BotaniaRecipeTypes.RUNIC_ALTAR_TYPE, input(items), level);
    }

    private static Optional<RecipeHolder<TerrestrialAgglomerationRecipe>> plate(Level level, Item... items) {
        return level.getRecipeManager().getRecipeFor(BotaniaRecipeTypes.TERRA_PLATE_TYPE, input(items), level);
    }

    private static Item[] concat(Item[] a, Item... b) {
        List<Item> all = new ArrayList<>(List.of(a));
        all.addAll(List.of(b));
        return all.toArray(Item[]::new);
    }

    // ---- Runic Altar ----

    @GameTest(template = ROOM)
    public static void runeOfLifeAndDeathFromGaiaIngotAndWildenTribute(GameTestHelper helper) {
        var recipe = altar(helper.getLevel(), BotaniaItems.GAIA_INGOT, ItemsRegistry.WILDEN_TRIBUTE.get()).orElseThrow();
        helper.assertTrue(recipe.value().getMana() == DivineConfig.settings().cost(RecipeCost.RUNE_OF_LIFE_AND_DEATH), "mana");
        helper.assertTrue(recipe.value().getResultItem(helper.getLevel().registryAccess()).is(ChainItems.RUNE_OF_LIFE_AND_DEATH.get()), "output");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void yinYangConsumesAllFourteenRunes(GameTestHelper helper) {
        Item[] virtues = new Item[7];
        for (VirtueRune v : VirtueRune.values()) virtues[v.ordinal()] = ChainItems.virtue(v);
        Item[] all = concat(SIN_RUNES, virtues);
        var recipe = altar(helper.getLevel(), all).orElseThrow();

        helper.assertTrue(recipe.value().getResultItem(helper.getLevel().registryAccess()).is(ChainItems.YIN_YANG.get()), "output");
        helper.assertTrue(recipe.value().getMana() == 1_000_000, "1M mana");
        helper.assertTrue(recipe.value().getCatalysts().isEmpty(), "no runes come back");
        helper.assertTrue(altar(helper.getLevel(), SIN_RUNES).isEmpty() || !altar(helper.getLevel(), SIN_RUNES).get().value()
                .getResultItem(helper.getLevel().registryAccess()).is(ChainItems.YIN_YANG.get()), "sins alone are not enough");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void timeComponentNeedsTheOverworld(GameTestHelper helper) {
        Item[] parts = {BotaniaBlocks.HOVERING_HOURGLASS.asItem(), Items.CLOCK, Items.ECHO_SHARD, ItemsRegistry.MANIPULATION_ESSENCE.get()};
        helper.assertTrue(altar(helper.getLevel(), parts).isPresent(), "overworld");
        Level nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(altar(nether, parts).isEmpty(), "not in the Nether");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void temporaryVirtueRunesUntilMichael(GameTestHelper helper) {
        var recipe = altar(helper.getLevel(), BotaniaItems.RUNE_OF_LUST, BotaniaItems.GAIA_SPIRIT).orElseThrow();
        helper.assertTrue(recipe.value().getResultItem(helper.getLevel().registryAccess()).is(ChainItems.virtue(VirtueRune.CHASTITY)), "lust becomes chastity");
        helper.succeed();
    }

    // ---- Terrestrial Agglomeration Plate ----

    @GameTest(template = ROOM)
    public static void spaceComponentOnlyInTheEnd(GameTestHelper helper) {
        Item[] parts = {BotaniaItems.PURE_ENDER_ESSENCE, ItemsRegistry.STABLE_WARP_SCROLL.get(), BotaniaItems.DRAGONSTONE, Items.END_CRYSTAL, Items.SHULKER_SHELL};
        helper.assertTrue(plate(helper.getLevel(), parts).isEmpty(), "the craft does not start outside the End");
        var end = plate(helper.getLevel().getServer().getLevel(Level.END), parts).orElseThrow();
        helper.assertTrue(end.value().getMana() == 10_000_000, "10M mana");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void divineIchorMakesAFullSetsWorth(GameTestHelper helper) {
        var recipe = plate(helper.getLevel(), ChainItems.RUNE_OF_LIFE_AND_DEATH.get(), ChainItems.YIN_YANG.get(),
                ChainItems.SPACETIME_FABRIC.get(), BotaniaItems.TERRASTEEL_INGOT, BotaniaItems.ELEMENTIUM_INGOT).orElseThrow();
        ItemStack out = recipe.value().getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(out.is(ChainItems.DIVINE_ICHOR.get()) && out.getCount() == 4, "4 Divine Ichor");
        helper.assertTrue(recipe.value().getMana() == 400_000_000, "400M mana");
        helper.succeed();
    }

    // ---- Enchanting Apparatus ----

    @GameTest(template = ROOM)
    public static void spacetimeFabricAtTheApparatus(GameTestHelper helper) {
        var recipe = IEnchantingRecipe.getRecipe(helper.getLevel(),
                new ApparatusRecipeInput(new ItemStack(ChainItems.SPACE_COMPONENT.get()), List.of(new ItemStack(ChainItems.TIME_COMPONENT.get())), null));
        helper.assertTrue(recipe != null, "recipe found");
        helper.assertTrue(recipe.value().sourceCost() == DivineConfig.settings().cost(RecipeCost.SPACETIME_FABRIC), "configured Source cost");
        helper.succeed();
    }

    private static ItemStack arsArmor(Item item, int arsTier) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentRegistry.ARMOR_PERKS, PerkUtil.getPerkHolder(stack).setTier(arsTier));
        return stack;
    }

    private static List<ItemStack> infusionPedestals() {
        List<ItemStack> pedestals = new ArrayList<>();
        pedestals.add(new ItemStack(ChainItems.DIVINE_ICHOR.get()));
        for (Item e : ESSENCES) pedestals.add(new ItemStack(e));
        return pedestals;
    }

    @GameTest(template = ROOM)
    public static void onlyTierThreeArsArmorTakesTheInfusion(GameTestHelper helper) {
        var tier3 = IEnchantingRecipe.getRecipe(helper.getLevel(), new ApparatusRecipeInput(arsArmor(ItemsRegistry.BATTLEMAGE_ROBES.get(), 2), infusionPedestals(), null));
        var tier1 = IEnchantingRecipe.getRecipe(helper.getLevel(), new ApparatusRecipeInput(arsArmor(ItemsRegistry.BATTLEMAGE_ROBES.get(), 0), infusionPedestals(), null));
        List<ItemStack> noWater = infusionPedestals();
        noWater.removeLast();
        var missing = IEnchantingRecipe.getRecipe(helper.getLevel(), new ApparatusRecipeInput(arsArmor(ItemsRegistry.BATTLEMAGE_ROBES.get(), 2), noWater, null));

        helper.assertTrue(tier3 != null && tier3.value() instanceof DivineArmorInfusionRecipe, "Tier 3 matches");
        helper.assertTrue(tier3.value().sourceCost() == 50_000, "50k Source");
        helper.assertTrue(tier1 == null || !(tier1.value() instanceof DivineArmorInfusionRecipe), "Tier 1 does not");
        helper.assertTrue(missing == null || !(missing.value() instanceof DivineArmorInfusionRecipe), "all 7 essences needed");
        helper.succeed();
    }

    /** End to end: a real apparatus, eight pedestals and the Divine Source Tank paying 50k Source. */
    @GameTest(template = ROOM, timeoutTicks = 400)
    public static void apparatusInfusesTierThreeRobesIntoDivineRobesFromTheTank(GameTestHelper helper) {
        BlockPos apparatus = new BlockPos(4, 2, 4);
        helper.setBlock(apparatus.below(), BlockRegistry.ARCANE_CORE_BLOCK.get());
        helper.setBlock(apparatus, BlockRegistry.ENCHANTING_APP_BLOCK.get());
        List<ItemStack> items = infusionPedestals();
        int i = 0;
        for (int dx = -2; dx <= 2; dx += 2) {
            for (int dz = -2; dz <= 2; dz += 2) {
                if (dx == 0 && dz == 0) continue;
                BlockPos p = apparatus.offset(dx, 0, dz);
                helper.setBlock(p, BlockRegistry.ARCANE_PEDESTAL.get());
                ((ArcanePedestalTile) helper.getBlockEntity(p)).setStack(items.get(i++));
            }
        }
        BlockPos tankPos = new BlockPos(0, 1, 0);
        helper.setBlock(tankPos, ModBlocks.DIVINE_SOURCE_TANK.get());
        DivineSourceTankTile tank = (DivineSourceTankTile) helper.getBlockEntity(tankPos);
        tank.setSource(60_000);

        EnchantingApparatusTile tile = (EnchantingApparatusTile) helper.getBlockEntity(apparatus);
        tile.setStack(arsArmor(ItemsRegistry.BATTLEMAGE_ROBES.get(), 2));
        helper.assertTrue(tile.attemptCraft(tile.getStack(), null), "the craft should start");

        helper.succeedWhen(() -> {
            ItemStack out = tile.getStack();
            helper.assertTrue(out.getItem() instanceof DivineArmorItem armor
                    && armor.piece().equals(new DivinePiece(ArcanistClass.BATTLEMAGE, ArmorSlot.CHEST)), "got " + out);
            helper.assertTrue(PerkUtil.getPerkHolder(out).getTier() == 0, "Divine has no tier");
            helper.assertTrue(tank.getSource() == 10_000, "the tank paid 50k, has " + tank.getSource());
        });
    }

    // ---- Divine Source Tank ----

    @GameTest(template = ROOM)
    public static void tankHoldsWhatTheInfusionNeedsAndAnArsJarCannot(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), ModBlocks.DIVINE_SOURCE_TANK.get());
        helper.setBlock(new BlockPos(7, 1, 7), BlockRegistry.SOURCE_JAR.get());
        DivineSourceTankTile tank = (DivineSourceTankTile) helper.getBlockEntity(new BlockPos(1, 1, 1));
        SourceJarTile jar = (SourceJarTile) helper.getBlockEntity(new BlockPos(7, 1, 7));

        tank.setSource(DivineConfig.settings().divineTankCapacity());
        jar.setSource(1_000_000);

        helper.assertTrue(tank.getSource() == DivineConfig.settings().divineTankCapacity(), "tank full at its capacity");
        helper.assertTrue(jar.getSource() == 10_000, "an Ars jar stops at 10k");
        helper.assertTrue(SourceUtil.hasSourceNearby(helper.absolutePos(new BlockPos(1, 2, 1)), helper.getLevel(), 2, 50_000),
                "Ars finds the tank as a Source provider");
        helper.succeed();
    }

    // ---- Time component ritual on a real Runic Altar ----

    private static RunicAltarBlockEntity timeAltar(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BotaniaBlocks.RUNIC_ALTAR);
        RunicAltarBlockEntity altar = (RunicAltarBlockEntity) helper.getBlockEntity(pos);
        for (Item item : new Item[]{BotaniaBlocks.HOVERING_HOURGLASS.asItem(), Items.CLOCK, Items.ECHO_SHARD, ItemsRegistry.MANIPULATION_ESSENCE.get()}) {
            altar.addItem(null, new ItemStack(item), null);
        }
        return altar;
    }

    // Time-of-day tests share the world clock, so each time of day gets its own batch (batches run in turn).

    @GameTest(template = ROOM, timeoutTicks = 60, batch = "time_ritual_dusk")
    public static void timeRitualStartsAtSunsetAndHoldsTheAltarShort(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        RunicAltarBlockEntity altar = timeAltar(helper, pos);
        helper.getLevel().setDayTime(12_500);

        // a flood of mana every tick for 15 ticks; checked once the altar has ticked again since
        for (int t = 1; t <= 15; t++) helper.runAfterDelay(t, () -> altar.receiveMana(Math.max(altar.manaToGet, 1)));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(altar.manaToGet == DivineConfig.settings().cost(RecipeCost.TIME_COMPONENT), "altar wants the time component's mana");
            helper.assertTrue(TimeRitualTracker.stateAt(helper.getLevel(), helper.absolutePos(pos)) == TimeRitual.State.RUNNING, "ritual running");
            helper.assertTrue(((RunicAltarAccessor) altar).arsbotania$getMana() == altar.manaToGet - 1, "held one short of finishing until sunrise");
            helper.succeed();
        });
    }

    @GameTest(template = ROOM, timeoutTicks = 60, batch = "time_ritual_day")
    public static void timeRitualRefusesToStartDuringTheDay(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        RunicAltarBlockEntity altar = timeAltar(helper, pos);
        helper.getLevel().setDayTime(6_000);

        helper.onEachTick(() -> altar.receiveMana(1_000));
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(TimeRitualTracker.stateAt(helper.getLevel(), helper.absolutePos(pos)) == null, "no ritual");
            helper.assertTrue(((RunicAltarAccessor) altar).arsbotania$getMana() <= 1_000, "mana is discarded, not stored");
            helper.succeed();
        });
    }

    @GameTest(template = ROOM, timeoutTicks = 100, batch = "time_ritual_dusk")
    public static void timeRitualFailsWhenTheManaStops(GameTestHelper helper) {
        DivineArmorSettings original = DivineConfig.settings();
        DivineConfig.overrideForTesting(original.toBuilder().timeRitualMaxGapTicks(10).build());
        BlockPos pos = new BlockPos(4, 1, 4);
        RunicAltarBlockEntity altar = timeAltar(helper, pos);
        helper.getLevel().setDayTime(12_500);
        BlockPos abs = helper.absolutePos(pos);

        // mana flows for 5 ticks, then stops
        for (int t = 1; t <= 5; t++) helper.runAfterDelay(t, () -> altar.receiveMana(5_000));
        helper.runAfterDelay(6, () -> helper.assertTrue(
                TimeRitualTracker.stateAt(helper.getLevel(), abs) == TimeRitual.State.RUNNING, "the ritual started"));
        helper.runAfterDelay(40, () -> {
            DivineConfig.overrideForTesting(original);
            helper.assertTrue(TimeRitualTracker.stateAt(helper.getLevel(), abs) == null, "the ritual ended");
            helper.assertTrue(((RunicAltarAccessor) altar).arsbotania$getMana() == 0, "the mana is lost");
            helper.assertTrue(!altar.getItemHandler().getItem(0).isEmpty(), "the ingredients stay in the altar");
            helper.succeed();
        });
    }
}
