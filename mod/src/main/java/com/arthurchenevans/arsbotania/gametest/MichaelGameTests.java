package com.arthurchenevans.arsbotania.gametest;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.VirtueRune;
import com.arthurchenevans.arsbotania.entity.MichaelEntity;
import com.arthurchenevans.arsbotania.entity.MichaelSummoning;
import com.arthurchenevans.arsbotania.registry.ChainItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.item.BotaniaItems;

/** Summoning Michael at a beacon, and the sin-to-virtue conversion when he dies. */
@GameTestHolder(ArsBotania.MODID)
@PrefixGameTestTemplate(false)
public class MichaelGameTests {
    private static final String ROOM = "empty9x9";
    private static final Item[] SINS = {BotaniaItems.RUNE_OF_LUST, BotaniaItems.RUNE_OF_GLUTTONY, BotaniaItems.RUNE_OF_GREED,
            BotaniaItems.RUNE_OF_SLOTH, BotaniaItems.RUNE_OF_WRATH, BotaniaItems.RUNE_OF_ENVY, BotaniaItems.RUNE_OF_PRIDE};

    /** A level-1 beacon: a 3x3 iron base with the beacon on top. Returns the beacon's relative position. */
    private static BlockPos beacon(GameTestHelper helper, boolean withBase) {
        BlockPos beacon = new BlockPos(4, 2, 4);
        if (withBase) {
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) helper.setBlock(beacon.offset(x, -1, z), Blocks.IRON_BLOCK);
        }
        helper.setBlock(beacon, Blocks.BEACON);
        return beacon;
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        player.moveTo(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
        return player;
    }

    @GameTest(template = ROOM)
    public static void gaiaIngotAtAnActiveBeaconSummonsMichael(GameTestHelper helper) {
        BlockPos beacon = beacon(helper, true);
        ServerPlayer player = player(helper);
        ItemStack ingot = new ItemStack(BotaniaItems.GAIA_INGOT, 2);

        MichaelEntity michael = MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), player, ingot);

        helper.assertTrue(michael != null, "Michael should appear");
        helper.assertTrue(ingot.getCount() == 1, "the ingot is used up");
        helper.assertTrue(michael.getMaxHealth() == DivineConfig.settings().michaelHealth(), "health from config");
        helper.assertTrue(michael.getY() > helper.absolutePos(beacon).getY(), "he appears above the beacon");
        michael.discard();
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void beaconWithoutAPyramidSummonsNothing(GameTestHelper helper) {
        BlockPos beacon = beacon(helper, false);
        ItemStack ingot = new ItemStack(BotaniaItems.GAIA_INGOT);

        helper.assertTrue(MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), player(helper), ingot) == null, "inactive beacon");
        helper.assertTrue(ingot.getCount() == 1, "ingot kept");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void onlyOneMichaelPerBeacon(GameTestHelper helper) {
        BlockPos beacon = beacon(helper, true);
        ServerPlayer player = player(helper);
        MichaelEntity first = MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), player, new ItemStack(BotaniaItems.GAIA_INGOT));

        helper.assertTrue(MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), player, new ItemStack(BotaniaItems.GAIA_INGOT)) == null,
                "a second Michael is refused");
        first.discard();
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void defeatingMichaelTurnsEachSinRuneIntoItsVirtue(GameTestHelper helper) {
        BlockPos beacon = beacon(helper, true);
        ServerPlayer fighter = player(helper);
        for (Item sin : SINS) fighter.getInventory().add(new ItemStack(sin, 2));
        MichaelEntity michael = MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), fighter, new ItemStack(BotaniaItems.GAIA_INGOT));

        michael.recordHit(fighter);
        michael.die(helper.getLevel().damageSources().playerAttack(fighter));

        for (VirtueRune v : VirtueRune.values()) {
            helper.assertTrue(fighter.getInventory().countItem(ChainItems.virtue(v)) == 1, "one " + v);
        }
        for (Item sin : SINS) helper.assertTrue(fighter.getInventory().countItem(sin) == 1, "one of each sin used");
        michael.discard();
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void aFighterMissingASinRuneGetsNothing(GameTestHelper helper) {
        BlockPos beacon = beacon(helper, true);
        ServerPlayer fighter = player(helper);
        for (int i = 0; i < SINS.length - 1; i++) fighter.getInventory().add(new ItemStack(SINS[i])); // no pride
        MichaelEntity michael = MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), fighter, new ItemStack(BotaniaItems.GAIA_INGOT));

        michael.recordHit(fighter);
        michael.die(helper.getLevel().damageSources().playerAttack(fighter));

        helper.assertTrue(fighter.getInventory().countItem(ChainItems.virtue(VirtueRune.CHASTITY)) == 0, "no conversion");
        helper.assertTrue(fighter.getInventory().countItem(BotaniaItems.RUNE_OF_LUST) == 1, "sin runes kept");
        michael.discard();
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void bystandersWhoNeverHitHimGetNothing(GameTestHelper helper) {
        BlockPos beacon = beacon(helper, true);
        ServerPlayer bystander = player(helper);
        for (Item sin : SINS) bystander.getInventory().add(new ItemStack(sin));
        MichaelEntity michael = MichaelSummoning.summon(helper.getLevel(), helper.absolutePos(beacon), bystander, new ItemStack(BotaniaItems.GAIA_INGOT));

        michael.die(helper.getLevel().damageSources().generic());

        helper.assertTrue(bystander.getInventory().countItem(ChainItems.virtue(VirtueRune.HUMILITY)) == 0, "no conversion");
        michael.discard();
        helper.succeed();
    }
}
