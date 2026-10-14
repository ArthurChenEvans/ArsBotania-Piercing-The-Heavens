package com.arthurchenevans.arsbotania.gametest;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.combo.DivineCombos;
import com.arthurchenevans.arsbotania.combo.GrimoireMana;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ComboOrder;
import com.arthurchenevans.arsbotania.core.divine.RodPower;
import com.arthurchenevans.arsbotania.item.DivineRodItem;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectBreak;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;

import java.util.List;

/** The Divine Grimoire's mana pool, the Divine Rod, and their left-then-right combos. */
@GameTestHolder(ArsBotania.MODID)
@PrefixGameTestTemplate(false)
public class GrimoireRodGameTests {
    private static final String ROOM = "empty9x9";

    private static ItemStack tablet(int mana) {
        ItemStack tablet = new ItemStack(BotaniaItems.MANA_TABLET);
        tablet.set(BotaniaDataComponents.MANA, mana);
        return tablet;
    }

    private static int tabletMana(ServerPlayer player) {
        return player.getInventory().getItem(8).getOrDefault(BotaniaDataComponents.MANA, 0);
    }

    // ---- Mana pool ----

    @GameTest(template = ROOM)
    public static void shortArsManaIsBoughtWithBotaniaMana(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        player.moveTo(helper.absoluteVec(new Vec3(4.5, 1, 4.5)));
        player.getInventory().setItem(8, tablet(100_000));
        ManaCap mana = CapabilityRegistry.getMana(player);
        mana.setMaxMana(500);
        mana.setMana(0);
        int cost = 40;

        GrimoireMana.topUp(player, new ItemStack(DivineItems.DIVINE_GRIMOIRE.get()), cost);

        helper.assertTrue(mana.getCurrentMana() >= cost, "Ars mana topped up to " + mana.getCurrentMana());
        int spent = 100_000 - tabletMana(player);
        helper.assertTrue(spent == cost * DivineConfig.settings().botaniaManaPerArsMana(), "Botania mana spent " + spent);
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void nearbySourceJarsArePreferredOverBotaniaMana(GameTestHelper helper) {
        BlockPos jarPos = new BlockPos(4, 1, 6);
        helper.setBlock(jarPos, BlockRegistry.SOURCE_JAR.get());
        ((SourceJarTile) helper.getBlockEntity(jarPos)).setSource(5_000);
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        player.moveTo(helper.absoluteVec(new Vec3(4.5, 1, 4.5)));
        player.getInventory().setItem(8, tablet(100_000));
        ManaCap mana = CapabilityRegistry.getMana(player);
        mana.setMaxMana(500);
        mana.setMana(0);
        int cost = 40;

        GrimoireMana.topUp(player, new ItemStack(DivineItems.DIVINE_GRIMOIRE.get()), cost);

        int jar = ((SourceJarTile) helper.getBlockEntity(jarPos)).getSource();
        helper.assertTrue(mana.getCurrentMana() >= cost, "Ars mana topped up");
        helper.assertTrue(jar == 5_000 - cost * DivineConfig.settings().sourcePerArsMana(), "jar now holds " + jar);
        helper.assertTrue(tabletMana(player) == 100_000, "Botania mana untouched while the jar can pay");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void nothingToPayWithMeansNoTopUp(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        player.moveTo(helper.absoluteVec(new Vec3(4.5, 1, 4.5)));
        ManaCap mana = CapabilityRegistry.getMana(player);
        mana.setMaxMana(500);
        mana.setMana(0);

        GrimoireMana.topUp(player, new ItemStack(DivineItems.DIVINE_GRIMOIRE.get()), 40);

        helper.assertTrue(mana.getCurrentMana() == 0, "no free mana");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void survivalCastWithNoArsManaIsPaidByBotaniaMana(GameTestHelper helper) {
        BlockPos stone = new BlockPos(4, 1, 4);
        helper.setBlock(stone, Blocks.STONE);
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        Vec3 feet = helper.absoluteVec(Vec3.atBottomCenterOf(stone.above()));
        player.moveTo(feet.x, feet.y, feet.z, 0, 90);
        player.getInventory().setItem(8, tablet(100_000));
        ItemStack book = new ItemStack(DivineItems.DIVINE_GRIMOIRE.get());
        book.update(DataComponentRegistry.SPELL_CASTER, book.get(DataComponentRegistry.SPELL_CASTER),
                c -> c.setSpell(new Spell(MethodTouch.INSTANCE, EffectBreak.INSTANCE)));
        player.setItemInHand(InteractionHand.MAIN_HAND, book);
        ManaCap mana = CapabilityRegistry.getMana(player);
        mana.setMaxMana(500); // Ars computes max mana as players tick; this test player never has
        mana.setMana(0);

        com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry.from(book)
                .castSpell(helper.getLevel(), player, InteractionHand.MAIN_HAND, null);

        helper.assertBlockPresent(Blocks.AIR, stone);
        helper.assertTrue(tabletMana(player) < 100_000, "the spell was paid with Botania mana");
        helper.succeed();
    }

    // ---- Divine Rod ----

    @GameTest(template = ROOM)
    public static void rodOfTheLandsPowerPlacesDirtForBotaniaMana(GameTestHelper helper) {
        BlockPos floor = new BlockPos(4, 1, 4);
        helper.setBlock(floor, Blocks.STONE);
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        player.moveTo(helper.absoluteVec(new Vec3(4.5, 2, 6.5)));
        player.getInventory().setItem(8, tablet(100_000));
        ItemStack rod = new ItemStack(DivineItems.DIVINE_ROD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, rod);
        BlockHitResult hit = new BlockHitResult(helper.absoluteVec(Vec3.atCenterOf(floor)).add(0, 0.5, 0), Direction.UP, helper.absolutePos(floor), false);

        rod.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));

        helper.assertBlockPresent(Blocks.DIRT, floor.above());
        helper.assertTrue(tabletMana(player) < 100_000, "the Lands power costs Botania mana");
        helper.succeed();
    }

    // ---- Combos ----

    /** A creative combo caster (creative casts Ars spells free) over a stone block, grimoire holds Touch + Break. */
    private static ServerPlayer comboCaster(GameTestHelper helper, BlockPos stone, boolean bookInLeftHand) {
        helper.setBlock(stone, Blocks.STONE);
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        player.setGameMode(GameType.CREATIVE);
        player.setMainArm(HumanoidArm.RIGHT);
        Vec3 feet = helper.absoluteVec(Vec3.atBottomCenterOf(stone.above()));
        player.moveTo(feet.x, feet.y, feet.z, 0, 90); // looking straight down at the stone
        ItemStack book = new ItemStack(DivineItems.DIVINE_GRIMOIRE.get());
        book.update(DataComponentRegistry.SPELL_CASTER, book.get(DataComponentRegistry.SPELL_CASTER),
                c -> c.setSpell(new Spell(MethodTouch.INSTANCE, EffectBreak.INSTANCE)));
        ItemStack rod = new ItemStack(DivineItems.DIVINE_ROD.get());
        DivineRodItem.setPower(rod, RodPower.LANDS);
        // right-handed: the off hand is the left hand
        player.setItemInHand(InteractionHand.OFF_HAND, bookInLeftHand ? book : rod);
        player.setItemInHand(InteractionHand.MAIN_HAND, bookInLeftHand ? rod : book);
        player.getInventory().setItem(8, tablet(100_000));
        return player;
    }

    @GameTest(template = ROOM)
    public static void bookLeftRodRightCastsThenUsesTheRod(GameTestHelper helper) {
        BlockPos stone = new BlockPos(4, 1, 4);
        ServerPlayer player = comboCaster(helper, stone, true);

        helper.assertTrue(DivineCombos.orderFor(player) == ComboOrder.SPELL_THEN_ROD, "order");
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.succeedWhen(() -> {
            helper.assertTrue(DivineCombos.lastSteps(player).equals(List.of(ComboOrder.Step.SPELL, ComboOrder.Step.ROD)),
                    "steps " + DivineCombos.lastSteps(player));
            // the spell broke the stone, then the rod filled the hole it left
            helper.assertBlockPresent(Blocks.DIRT, stone);
            helper.assertBlockPresent(Blocks.AIR, stone.above());
        });
    }

    @GameTest(template = ROOM)
    public static void rodLeftBookRightUsesTheRodThenCasts(GameTestHelper helper) {
        BlockPos stone = new BlockPos(4, 1, 4);
        ServerPlayer player = comboCaster(helper, stone, false);

        helper.assertTrue(DivineCombos.orderFor(player) == ComboOrder.ROD_THEN_SPELL, "order");
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.succeedWhen(() -> {
            helper.assertTrue(DivineCombos.lastSteps(player).equals(List.of(ComboOrder.Step.ROD, ComboOrder.Step.SPELL)),
                    "steps " + DivineCombos.lastSteps(player));
            helper.assertBlockPresent(Blocks.DIRT, stone.above());  // the rod built first
            helper.assertBlockPresent(Blocks.AIR, stone);           // then the spell landed on the block it hit
        });
    }

    @GameTest(template = ROOM)
    public static void leftHandedPlayersComboLeftThenRightToo(GameTestHelper helper) {
        ServerPlayer player = comboCaster(helper, new BlockPos(4, 1, 4), true);
        player.setMainArm(HumanoidArm.LEFT); // now the main hand (rod) is the left hand

        helper.assertTrue(DivineCombos.orderFor(player) == ComboOrder.ROD_THEN_SPELL, "left-handed order");
        helper.succeed();
    }
}
