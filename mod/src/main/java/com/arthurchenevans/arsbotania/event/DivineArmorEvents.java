package com.arthurchenevans.arsbotania.event;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.Bonuses;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.DivineBonuses;
import com.arthurchenevans.arsbotania.core.divine.DivineLoadout;
import com.arthurchenevans.arsbotania.core.divine.DivinePiece;
import com.arthurchenevans.arsbotania.core.divine.HealthRegen;
import com.arthurchenevans.arsbotania.registry.DivineItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import vazkii.botania.api.mana.ManaItemHandler;

/** Per-tick Divine armor effects: Botania mana regen and health regen. */
@EventBusSubscriber(modid = ArsBotania.MODID)
public final class DivineArmorEvents {
    private DivineArmorEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        DivineLoadout loadout = DivineItems.loadoutOf(player);
        if (loadout.count() == 0) return;

        DivineArmorSettings settings = DivineConfig.settings();
        Bonuses bonuses = DivineBonuses.of(loadout, settings);

        if (bonuses.botaniaManaPerTick() > 0) {
            // Botania ignores dispatches from an empty stack, so a worn Divine piece is the source.
            ItemStack source = wornDivinePiece(player, loadout);
            ManaItemHandler.instance().dispatchMana(source, player, bonuses.botaniaManaPerTick(), true);
        }

        float heal = new HealthRegen(settings).healAt(player.level().getGameTime(), bonuses, player.getHealth(), player.getMaxHealth());
        if (heal > 0) player.heal(heal);
    }

    private static ItemStack wornDivinePiece(Player player, DivineLoadout loadout) {
        DivinePiece piece = loadout.pieces().iterator().next();
        return player.getItemBySlot(DivineItems.toEquipmentSlot(piece.slot()));
    }
}
