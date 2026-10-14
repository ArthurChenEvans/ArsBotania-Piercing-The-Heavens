package com.arthurchenevans.arsbotania.gametest;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivineBlade;
import com.arthurchenevans.arsbotania.event.DivineBladeEvents;
import com.arthurchenevans.arsbotania.mixin.PixieEntityAccessor;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIgnite;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.entity.FallingStarEntity;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.entity.PixieEntity;

/** Each folded-in blade ability, in a real world. */
@GameTestHolder(ArsBotania.MODID)
@PrefixGameTestTemplate(false)
public class DivineBladeGameTests {
    private static final String ROOM = "empty9x9";

    private static ServerPlayer swordsman(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survivalServerPlayer(helper);
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 4.5));
        player.moveTo(at.x, at.y, at.z, -90, 0); // facing east, into the room
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DivineItems.DIVINE_BLADE.get()));
        return player;
    }

    private static Zombie zombie(GameTestHelper helper, int x, int z) {
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, x, 1, z);
        zombie.setNoAi(true);
        return zombie;
    }

    @GameTest(template = ROOM)
    public static void thundercallerLightningChainsToNearbyEnemies(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        Zombie hit = zombie(helper, 3, 4), nearA = zombie(helper, 5, 4), nearB = zombie(helper, 5, 6);
        float before = nearA.getHealth() + nearB.getHealth();

        player.getMainHandItem().hurtEnemy(hit, player);

        helper.assertTrue(nearA.getHealth() + nearB.getHealth() < before, "lightning should chain to the other zombies");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void fullStrengthSwingFiresATerraBeam(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);

        DivineBladeEvents.trySwingBeam(player, 1.0f);

        helper.assertFalse(helper.getLevel().getEntitiesOfClass(ManaBurstEntity.class, player.getBoundingBox().inflate(4)).isEmpty(),
                "a mana burst should leave the blade");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void weakSwingFiresNothing(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);

        DivineBladeEvents.trySwingBeam(player, 0.5f);

        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ManaBurstEntity.class, player.getBoundingBox().inflate(4)).isEmpty(),
                "no burst below full strength");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void swingingCallsAStarcallerStar(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) for (int z = 0; z < 9; z++) helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
        ServerPlayer player = swordsman(helper);
        player.setXRot(60); // looking at the floor
        ItemStack blade = player.getMainHandItem();
        blade.set(BotaniaDataComponents.LAST_TRIGGER_TIME, helper.getLevel().getGameTime() - 20);
        player.attackAnim = 1F / 6F; // the swing frame Botania's Starcaller fires on

        blade.inventoryTick(helper.getLevel(), player, 0, true);

        helper.assertFalse(helper.getLevel().getEntitiesOfClass(FallingStarEntity.class, player.getBoundingBox().inflate(48)).isEmpty(),
                "a falling star should be called");
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void pixiesHitHarderLikeWithTheElementiumSword(GameTestHelper helper) {
        DivineArmorSettings original = DivineConfig.settings();
        DivineArmorSettings.Builder always = original.toBuilder();
        for (ArmorSlot slot : ArmorSlot.values()) always.pixieChance(slot, 1.0);
        DivineConfig.overrideForTesting(always.build());
        try {
            // A plain mock player: fresh server players ignore damage for their first 60 ticks.
            net.minecraft.world.entity.player.Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.moveTo(helper.absoluteVec(new Vec3(1.5, 1, 4.5)));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DivineItems.DIVINE_BLADE.get()));
            for (ArmorSlot slot : ArmorSlot.values()) {
                player.setItemSlot(DivineItems.toEquipmentSlot(slot), new ItemStack(DivineItems.get(
                        new com.arthurchenevans.arsbotania.core.divine.DivinePiece(com.arthurchenevans.arsbotania.core.divine.ArcanistClass.SORCERER, slot))));
            }
            Zombie attacker = zombie(helper, 3, 4);
            player.tick();
            player.hurt(player.damageSources().mobAttack(attacker), 1f);

            var pixies = helper.getLevel().getEntitiesOfClass(PixieEntity.class, player.getBoundingBox().inflate(5));
            helper.assertFalse(pixies.isEmpty(), "a pixie should be summoned");
            float damage = ((PixieEntityAccessor) pixies.getFirst()).arsbotania$getDamage();
            helper.assertTrue(damage == DivineBlade.pixieDamage(true), "pixie damage " + damage);
            pixies.forEach(p -> p.discard());
        } finally {
            DivineConfig.overrideForTesting(original);
        }
        helper.succeed();
    }

    @GameTest(template = ROOM)
    public static void bladeCastsItsOwnArsSpellOnHit(GameTestHelper helper) {
        ServerPlayer player = swordsman(helper);
        player.setGameMode(GameType.CREATIVE); // creative casts without needing Ars mana
        ItemStack blade = player.getMainHandItem();
        blade.update(DataComponentRegistry.SPELL_CASTER, blade.get(DataComponentRegistry.SPELL_CASTER),
                caster -> caster.setSpell(new Spell(MethodTouch.INSTANCE, EffectIgnite.INSTANCE))); // as scribing leaves it
        Zombie target = zombie(helper, 3, 4);

        blade.hurtEnemy(target, player);

        helper.succeedWhen(() -> helper.assertTrue(target.isOnFire(), "the blade's Ignite spell should set the target alight"));
    }
}
