package com.arthurchenevans.arsbotania.gametest;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.item.SingularityItem;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Singularity in a real world: S-rank Terra Shatterer and Terra Truncator behaviour. */
@GameTestHolder(ArsBotania.MODID)
@PrefixGameTestTemplate(false)
public class SingularityGameTests {
    private static final String ROOM = "empty9x9";

    /** A server player standing over {@code below}, looking straight down at it, holding the Singularity. */
    private static ServerPlayer minerAbove(GameTestHelper helper, BlockPos below, boolean areaMode) {
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        Vec3 feet = helper.absoluteVec(Vec3.atBottomCenterOf(below.above()));
        player.moveTo(feet.x, feet.y, feet.z, 0, 90);
        ItemStack singularity = new ItemStack(DivineItems.SINGULARITY.get());
        SingularityItem.setAreaMode(singularity, areaMode);
        player.setItemInHand(InteractionHand.MAIN_HAND, singularity);
        return player;
    }

    private static void fill(GameTestHelper helper, int y, int radius, BlockPos centre) {
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                helper.setBlock(centre.offset(x, y, z), Blocks.STONE);
    }

    @GameTest(template = ROOM)
    public static void sRankShattersASevenBySevenLayer(GameTestHelper helper) {
        BlockPos centre = new BlockPos(4, 1, 4);
        fill(helper, 0, 4, centre); // 9x9, so the outer ring proves the area stops at 7x7
        ServerPlayer player = minerAbove(helper, centre, true);

        player.gameMode.destroyBlock(helper.absolutePos(centre));

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                boolean inside = Math.abs(x) <= 3 && Math.abs(z) <= 3;
                BlockPos p = centre.offset(x, 0, z);
                if (inside) helper.assertBlockPresent(Blocks.AIR, p);
                else helper.assertBlockPresent(Blocks.STONE, p);
            }
        }
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void areaModeOffBreaksOneBlock(GameTestHelper helper) {
        BlockPos centre = new BlockPos(4, 1, 4);
        fill(helper, 0, 1, centre);
        ServerPlayer player = minerAbove(helper, centre, false);

        player.gameMode.destroyBlock(helper.absolutePos(centre));

        helper.assertBlockPresent(Blocks.AIR, centre);
        helper.assertBlockPresent(Blocks.STONE, centre.east());
        helper.succeed();
    }

    @GameTest(template = ROOM, timeoutTicks = 100)
    public static void hittingTheTrunkFellsTheTree(GameTestHelper helper) {
        BlockPos base = new BlockPos(4, 1, 4);
        for (int y = 0; y < 5; y++) helper.setBlock(base.above(y), Blocks.OAK_LOG);
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        Vec3 eye = helper.absoluteVec(Vec3.atCenterOf(base.north(2)));
        player.moveTo(eye.x, eye.y - player.getEyeHeight(), eye.z, 0, 0); // facing south, at the base log
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DivineItems.SINGULARITY.get()));

        player.gameMode.destroyBlock(helper.absolutePos(base));

        helper.succeedWhen(() -> {
            for (int y = 0; y < 5; y++) helper.assertBlockPresent(Blocks.AIR, base.above(y));
        });
    }

    @GameTest(template = ROOM)
    public static void minesAsPickaxeAxeShovelAndHoeFasterThanTerrasteel(GameTestHelper helper) {
        ItemStack s = new ItemStack(DivineItems.SINGULARITY.get());

        helper.assertTrue(s.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "pickaxe at netherite level");
        helper.assertTrue(s.isCorrectToolForDrops(Blocks.ANCIENT_DEBRIS.defaultBlockState()), "ancient debris");
        helper.assertTrue(s.getDestroySpeed(Blocks.STONE.defaultBlockState()) > 9f, "faster than Terrasteel (9)");
        helper.assertTrue(s.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) > 9f, "axe");
        helper.assertTrue(s.getDestroySpeed(Blocks.DIRT.defaultBlockState()) > 9f, "shovel");
        helper.assertTrue(s.getDestroySpeed(Blocks.HAY_BLOCK.defaultBlockState()) > 9f, "hoe");
        helper.succeed();
    }
}
