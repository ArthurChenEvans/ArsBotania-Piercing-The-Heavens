package com.arthurchenevans.arsbotania.entity;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.Michael;
import com.arthurchenevans.arsbotania.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import vazkii.botania.common.item.BotaniaItems;

/**
 * Summoning ritual: right-click an active beacon with a Gaia Spirit Ingot. The ingot is used
 * up and Michael appears above the beacon; he can be summoned again, like the Gaia Guardian.
 */
@EventBusSubscriber(modid = ArsBotania.MODID)
public final class MichaelSummoning {
    private MichaelSummoning() {
    }

    @SubscribeEvent
    public static void onUseBeacon(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!held.is(BotaniaItems.GAIA_INGOT) || !level.getBlockState(pos).is(Blocks.BEACON)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide()) return;
        summon((ServerLevel) level, pos, event.getEntity(), held);
    }

    /** Returns the summoned Michael, or null if the beacon isn't active or he's already there. */
    public static MichaelEntity summon(ServerLevel level, BlockPos beacon, Player player, ItemStack ingot) {
        boolean alreadyThere = !level.getEntitiesOfClass(MichaelEntity.class, new AABB(beacon).inflate(32),
                m -> beacon.equals(m.home())).isEmpty();
        if (!Michael.canSummon(beaconLevels(level, beacon), alreadyThere)) return null;
        MichaelEntity michael = ModEntities.MICHAEL.get().create(level);
        if (michael == null) return null;
        michael.summonAt(beacon);
        level.addFreshEntity(michael);
        if (!player.isCreative()) ingot.shrink(1);
        return michael;
    }

    /** How many full pyramid layers of beacon base blocks sit under the beacon (0 to 4). */
    public static int beaconLevels(Level level, BlockPos beacon) {
        int levels = 0;
        for (int layer = 1; layer <= 4; layer++) {
            int y = beacon.getY() - layer;
            for (int x = -layer; x <= layer; x++) {
                for (int z = -layer; z <= layer; z++) {
                    if (!level.getBlockState(new BlockPos(beacon.getX() + x, y, beacon.getZ() + z)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        return levels;
                    }
                }
            }
            levels = layer;
        }
        return levels;
    }
}
