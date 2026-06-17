package net.minecraftfr.roleplaychat.test.client;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NameplateDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;

    // UUIDs fixes pour skins déterministes entre les runs (Steve par défaut car hashCode pair)
    private static final UUID UUID_WITH_RP   = UUID.fromString("cafebabe-0000-0000-0000-000000000001");
    private static final UUID UUID_WITHOUT_RP = UUID.fromString("cafebabe-0000-0000-0000-000000000002");

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        testNameplateWithRpName(context);
        testNameplateWithoutRpName(context);
    }

    private static void testNameplateWithRpName(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            spawnMockPlayerInFront(sp, context, "MockPlayer", UUID_WITH_RP);
            context.runOnClient(mc -> RpNameClientCache.set(UUID_WITH_RP, "Elara"));
            context.waitTicks(5);
            takeCleanScreenshot(context, "nameplate_with_rp_name");
        }
    }

    private static void testNameplateWithoutRpName(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            spawnMockPlayerInFront(sp, context, "MockPlayer", UUID_WITHOUT_RP);
            takeCleanScreenshot(context, "nameplate_without_rp_name");
        }
    }

    private static void takeCleanScreenshot(ClientGameTestContext context, String name) {
        // Vider le chat pour éviter le message "MockPlayer joined the game"
        context.runOnClient(mc -> mc.inGameHud.getChatHud().clear(false));
        context.waitTicks(2);
        context.assertScreenshotEquals(name);
    }

    private static void spawnMockPlayerInFront(TestSingleplayerContext sp, ClientGameTestContext context, String name, UUID uuid) {
        context.waitFor(mc -> mc.player != null && mc.player.getY() != 0);

        double[] localPos = new double[3];
        context.runOnClient(mc -> {
            localPos[0] = mc.player.getX();
            localPos[1] = mc.player.getY();
            localPos[2] = mc.player.getZ();
            mc.options.setPerspective(Perspective.FIRST_PERSON);
            mc.player.setYaw(0.0f);
            mc.player.setPitch(-20.0f);
        });

        sp.getServer().runOnServer(server -> {
            var world = server.getOverworld();

            world.setTimeOfDay(6000L);
            server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
            server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
            world.resetWeather();

            GameProfile profile = new GameProfile(uuid, name);
            ConnectedClientData data = ConnectedClientData.createDefault(profile, false);
            ServerPlayerEntity mock = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());

            ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
            new EmbeddedChannel(connection);
            server.getPlayerManager().onPlayerConnect(connection, mock, data);

            // 3 blocs devant le joueur local (direction sud +Z), face à lui
            mock.setPos(localPos[0], localPos[1], localPos[2] + 3.0);
            mock.setYaw(180.0f);
            mock.setPitch(0.0f);
        });

        context.waitTicks(20);
    }
}
