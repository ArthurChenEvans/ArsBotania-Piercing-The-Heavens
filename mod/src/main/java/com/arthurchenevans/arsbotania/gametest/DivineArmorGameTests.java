package com.arthurchenevans.arsbotania.gametest;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.ArcanistClass;
import com.arthurchenevans.arsbotania.core.divine.ArmorSlot;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.api.item.AncientWillContainer;
import com.arthurchenevans.arsbotania.event.DivineArmorEvents;
import com.arthurchenevans.arsbotania.mixin.PixieEntityAccessor;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

/** In-world checks on a real player. Run with {@code ./gradlew :mod:runGameTestServer}. */
@GameTestHolder(ArsBotania.MODID)
@PrefixGameTestTemplate(false)
public class DivineArmorGameTests {
    private static final String EMPTY = "empty3x3";

    private static Player playerWearing(GameTestHelper helper, int pieces) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ArmorSlot[] slots = ArmorSlot.values();
        for (int i = 0; i < pieces; i++) {
            DivinePiece piece = new DivinePiece(ArcanistClass.BATTLEMAGE, slots[i]);
            player.setItemSlot(DivineItems.toEquipmentSlot(slots[i]), new ItemStack(DivineItems.get(piece)));
        }
        return player;
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void fullSetRegeneratesHealth(GameTestHelper helper) {
        Player player = playerWearing(helper, 4);
        player.setHealth(5f);

        helper.onEachTick(player::tick);
        helper.succeedWhen(() -> helper.assertTrue(player.getHealth() > 5f, "expected Divine armor to heal"));
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void noArmorNoRegeneration(GameTestHelper helper) {
        Player player = playerWearing(helper, 0);
        player.setHealth(5f);

        helper.onEachTick(player::tick);
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(player.getHealth() <= 5f, "healed without Divine armor");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void botaniaSeesTheDivineToolDiscount(GameTestHelper helper) {
        DivineArmorSettings s = DivineConfig.settings();

        float full = ManaItemHandler.instance().getFullDiscountForTools(playerWearing(helper, 4), ItemStack.EMPTY);
        float two = ManaItemHandler.instance().getFullDiscountForTools(playerWearing(helper, 2), ItemStack.EMPTY);

        float expectedFull = (float) (4 * s.botaniaToolDiscountPerPiece() + s.terrasteelSetDiscount() + s.manaweaveSetDiscount());
        float expectedTwo = (float) (2 * s.botaniaToolDiscountPerPiece());
        helper.assertTrue(Math.abs(full - expectedFull) < 1e-4, "full set discount " + full + " != " + expectedFull);
        helper.assertTrue(Math.abs(two - expectedTwo) < 1e-4, "partial discount " + two + " != " + expectedTwo);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void divineArmorFillsBotaniaManaItems(GameTestHelper helper) {
        Player player = playerWearing(helper, 1);
        player.getInventory().setItem(0, new ItemStack(BotaniaItems.MANA_TABLET));

        helper.onEachTick(player::tick);
        helper.succeedWhen(() -> {
            int mana = player.getInventory().getItem(0).getOrDefault(BotaniaDataComponents.MANA, 0);
            helper.assertTrue(mana > 0, "mana tablet did not receive Botania mana");
        });
    }

    @GameTest(template = EMPTY)
    public static void fullSetMakesBotaniaRodsProficient(GameTestHelper helper) {
        ItemStack rod = new ItemStack(BotaniaItems.ROD_OF_THE_LANDS);

        helper.assertTrue(ManaItemHandler.instance().hasProficiency(playerWearing(helper, 4), rod), "full set should give rod proficiency");
        helper.assertFalse(ManaItemHandler.instance().hasProficiency(playerWearing(helper, 3), rod), "partial set should not");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void fullSetHealsWhileHungry(GameTestHelper helper) {
        // Only the Terrasteel bonus heals: switch off Divine's own health regen.
        DivineArmorSettings original = DivineConfig.settings();
        DivineConfig.overrideForTesting(original.toBuilder().healthRegenPerPiece(0).build());
        Player player = playerWearing(helper, 4);
        player.getFoodData().setFoodLevel(10);
        player.setHealth(10f);

        helper.onEachTick(player::tick);
        helper.succeedWhen(() -> {
            helper.assertTrue(player.getHealth() > 10f, "Terrasteel set bonus should heal while hungry");
            DivineConfig.overrideForTesting(original);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void fullSetPixiesApplyPotions(GameTestHelper helper) {
        DivineArmorSettings original = DivineConfig.settings();
        DivineArmorSettings.Builder always = original.toBuilder();
        for (ArmorSlot slot : ArmorSlot.values()) always.pixieChance(slot, 1.0);
        DivineConfig.overrideForTesting(always.build());

        Player player = playerWearing(helper, 4);
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 1, 1)));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 1, 1, 1);
        player.tick(); // armor attributes (pixie chance) apply on tick
        player.hurt(player.damageSources().mobAttack(zombie), 1f);
        DivineConfig.overrideForTesting(original);

        // Botania spawns the pixie during the hit, so check straight away, before it flies off.
        var pixies = helper.getLevel().getEntitiesOfClass(PixieEntity.class, player.getBoundingBox().inflate(5));
        helper.assertFalse(pixies.isEmpty(), "taking a hit should summon a pixie");
        helper.assertTrue(((PixieEntityAccessor) pixies.getFirst()).arsbotania$getEffect() != null,
                "full-set pixies should carry a potion effect");
        pixies.forEach(p -> p.discard());
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void hoodWillsApplyOnCriticalHits(GameTestHelper helper) {
        Player player = playerWearing(helper, 4);
        ItemStack hood = player.getItemBySlot(EquipmentSlot.HEAD);
        ((AncientWillContainer) hood.getItem()).addAncientWill(hood, AncientWillContainer.AncientWillType.KARIL);
        Zombie target = helper.spawn(EntityType.ZOMBIE, 1, 1, 1);

        DivineArmorEvents.onCriticalHit(new CriticalHitEvent(player, target, 1.5f, true));
        target.hurt(player.damageSources().playerAttack(player), 2f);

        helper.succeedWhen(() -> helper.assertTrue(target.hasEffect(MobEffects.WITHER), "Karil should wither the target"));
    }
}
