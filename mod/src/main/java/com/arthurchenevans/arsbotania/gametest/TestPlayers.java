package com.arthurchenevans.arsbotania.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.UUID;

/** Test players for GameTests. */
final class TestPlayers {
    private TestPlayers() {
    }

    /**
     * A survival server player with a working (but unconnected) network handler, in the test's level.
     * Unlike {@link GameTestHelper#makeMockServerPlayerInLevel()} it is not joined to the player list,
     * so mods that sync data on join (Curios) don't try to talk to a client that isn't there,
     * and it is not forced into creative.
     */
    static ServerPlayer survivalServerPlayer(GameTestHelper helper) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-survival-player"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie);
        return player;
    }
}
