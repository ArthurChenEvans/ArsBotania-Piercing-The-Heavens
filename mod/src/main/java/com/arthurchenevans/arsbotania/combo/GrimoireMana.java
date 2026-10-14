package com.arthurchenevans.arsbotania.combo;

import com.arthurchenevans.arsbotania.config.DivineConfig;
import com.arthurchenevans.arsbotania.core.divine.DivineArmorSettings;
import com.arthurchenevans.arsbotania.core.divine.ManaBridge;
import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.capability.ManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.ManaItemHandler;

/**
 * The Divine Grimoire's shared pool: tops up a short Ars mana bar before a spell's mana check,
 * buying the gap from nearby Source jars first, then Botania mana. Each is paid in full.
 */
public final class GrimoireMana {
    private GrimoireMana() {
    }

    public static void topUp(Player player, ItemStack grimoire, int cost) {
        if (player.level().isClientSide()) return;
        ManaCap mana = CapabilityRegistry.getMana(player);
        if (mana == null) return;
        int need = ManaBridge.deficit(cost, mana.getCurrentMana(), mana.getMaxMana());
        if (need <= 0) return;
        DivineArmorSettings s = DivineConfig.settings();

        need -= fromJars(player, need, s);
        if (need > 0) fromBotania(player, grimoire, need, s);
        mana.setMana(Math.min(mana.getMaxMana(), mana.getCurrentMana()));
        if (player instanceof ServerPlayer sp && sp.connection != null) mana.syncToClient(sp);
    }

    /** Takes what Source the nearby jars can give, up to the need; returns Ars mana bought. */
    private static int fromJars(Player player, int need, DivineArmorSettings s) {
        int available = 0;
        for (ISpecialSourceProvider p : SourceUtil.canTakeSource(player.blockPosition(), player.level(), s.jarRange())) {
            available += p.getSource().getSource();
        }
        int bought = Math.min(need, available / s.sourcePerArsMana());
        if (bought <= 0 || SourceUtil.takeSourceMultiple(player.blockPosition(), player.level(), s.jarRange(), ManaBridge.sourceCost(bought, s)) == null) {
            return 0;
        }
        CapabilityRegistry.getMana(player).addMana(bought);
        return bought;
    }

    /** Takes Botania mana from the player's mana items, up to the need; returns Ars mana bought. */
    private static int fromBotania(Player player, ItemStack grimoire, int need, DivineArmorSettings s) {
        int got = ManaItemHandler.instance().requestMana(grimoire, player, ManaBridge.botaniaCost(need, s), false);
        int bought = got / s.botaniaManaPerArsMana();
        if (bought <= 0) return 0;
        ManaItemHandler.instance().requestManaExact(grimoire, player, ManaBridge.botaniaCost(bought, s), true);
        CapabilityRegistry.getMana(player).addMana(bought);
        return bought;
    }
}
