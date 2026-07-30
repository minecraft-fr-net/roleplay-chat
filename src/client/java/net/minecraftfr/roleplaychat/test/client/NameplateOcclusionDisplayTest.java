package net.minecraftfr.roleplaychat.test.client;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NameplateOcclusionDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;
    private static final UUID UUID_OCCLUDED    = UUID.fromString("cafebabe-0000-0000-0000-000000000003");
    private static final UUID UUID_NO_WALL     = UUID.fromString("cafebabe-0000-0000-0000-000000000004");
    private static final UUID UUID_CREATIVE    = UUID.fromString("cafebabe-0000-0000-0000-000000000005");

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        testNameplateHiddenBehindWall(context);
        testNameplateVisibleWithoutWall(context);
        testNameplateVisibleThroughWallInCreative(context);
    }

    private static void testNameplateVisibleWithoutWall(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            double[] localPos = new double[3];

            context.waitFor(mc -> mc.player != null && mc.player.getY() != 0);
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

                GameProfile profile = new GameProfile(UUID_NO_WALL, "Visible");
                ConnectedClientData data = ConnectedClientData.createDefault(profile, false);
                ServerPlayerEntity mock = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
                ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerManager().onPlayerConnect(connection, mock, data);
                mock.setPos(localPos[0], localPos[1], localPos[2] + 4.0);
                mock.setYaw(180.0f);
                mock.setPitch(0.0f);
            });

            context.waitTicks(20);
            context.runOnClient(mc -> RpNameClientCache.set(UUID_NO_WALL, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.inGameHud.getChatHud().clear(false));
            context.waitTicks(2);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("nameplate_visible_no_wall")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }

    private static void testNameplateVisibleThroughWallInCreative(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            double[] localPos = new double[3];

            context.waitFor(mc -> mc.player != null && mc.player.getY() != 0);
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

                // Même mur que le test d'occlusion
                BlockPos wallBase = new BlockPos(
                    (int) Math.floor(localPos[0]),
                    (int) Math.floor(localPos[1]),
                    (int) Math.floor(localPos[2]) + 1
                );
                BlockState stone = Blocks.STONE.getDefaultState();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        world.setBlockState(wallBase.add(dx, dy, 0), stone);
                    }
                }

                GameProfile profile = new GameProfile(UUID_CREATIVE, "WallCreative");
                ConnectedClientData data = ConnectedClientData.createDefault(profile, false);
                ServerPlayerEntity mock = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
                ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerManager().onPlayerConnect(connection, mock, data);
                mock.setPos(localPos[0], localPos[1], localPos[2] + 4.0);
                mock.setYaw(180.0f);
                mock.setPitch(0.0f);
            });

            // Passer en Creative via le serveur pour que le HUD et les abilities soient corrects
            // Les mock players ont des UUIDs cafebabe-*, le joueur local a un UUID différent
            sp.getServer().runOnServer(server ->
                server.getPlayerManager().getPlayerList().stream()
                    .filter(p -> !p.getUuidAsString().startsWith("cafebabe"))
                    .findFirst()
                    .ifPresent(p -> p.changeGameMode(GameMode.CREATIVE))
            );
            // Attendre que le mode Creative soit effectivement appliqué côté client
            context.waitFor(mc -> mc.player.getAbilities().creativeMode);

            context.runOnClient(mc -> RpNameClientCache.set(UUID_CREATIVE, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.inGameHud.getChatHud().clear(false));
            context.waitTicks(2);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("nameplate_visible_creative_through_wall")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }

    private static void testNameplateHiddenBehindWall(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            double[] localPos = new double[3];

            context.waitFor(mc -> mc.player != null && mc.player.getY() != 0);
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

                // Mur de stone entre la caméra et le mock player (z+1), 3 blocs de large, 4 de haut
                BlockPos wallBase = new BlockPos(
                    (int) Math.floor(localPos[0]),
                    (int) Math.floor(localPos[1]),
                    (int) Math.floor(localPos[2]) + 1
                );
                BlockState stone = Blocks.STONE.getDefaultState();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        world.setBlockState(wallBase.add(dx, dy, 0), stone);
                    }
                }

                // Mock player derrière le mur à z+4
                GameProfile profile = new GameProfile(UUID_OCCLUDED, "BehindWall");
                ConnectedClientData data = ConnectedClientData.createDefault(profile, false);
                ServerPlayerEntity mock = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
                ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerManager().onPlayerConnect(connection, mock, data);
                mock.setPos(localPos[0], localPos[1], localPos[2] + 4.0);
                mock.setYaw(180.0f);
                mock.setPitch(0.0f);
            });

            context.waitTicks(20);
            // Activer un pseudo RP pour s'assurer que le nametag serait visible sans occlusion
            context.runOnClient(mc -> RpNameClientCache.set(UUID_OCCLUDED, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.inGameHud.getChatHud().clear(false));
            context.waitTicks(2);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("nameplate_occluded_by_wall")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }
}
