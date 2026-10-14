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

        float expectedFull = (float) (4 * s.botaniaToolDiscountPerPiece() + s.fullSetBotaniaToolDiscount());
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
}
