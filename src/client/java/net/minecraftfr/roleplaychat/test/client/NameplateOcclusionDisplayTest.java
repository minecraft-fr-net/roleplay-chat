package net.minecraftfr.roleplaychat.test.client;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.CameraType;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
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
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.player.setYRot(0.0f);
                mc.player.setXRot(-20.0f);
            });

            sp.getServer().runOnServer(server -> {
                var world = server.overworld();

                world.setDayTime(6000L);
                world.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
                world.getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
                world.resetWeatherCycle();

                GameProfile profile = new GameProfile(UUID_NO_WALL, "Visible");
                CommonListenerCookie data = CommonListenerCookie.createInitial(profile, false);
                ServerPlayer mock = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
                Connection connection = new Connection(PacketFlow.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerList().placeNewPlayer(connection, mock, data);
                mock.setPos(localPos[0], localPos[1], localPos[2] + 4.0);
                mock.setYRot(180.0f);
                mock.setXRot(0.0f);
            });

            context.waitTicks(20);
            context.runOnClient(mc -> RpNameClientCache.set(UUID_NO_WALL, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.gui.getChat().clearMessages(false));
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
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.player.setYRot(0.0f);
                mc.player.setXRot(-20.0f);
            });

            sp.getServer().runOnServer(server -> {
                var world = server.overworld();

                world.setDayTime(6000L);
                world.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
                world.getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
                world.resetWeatherCycle();

                // Même mur que le test d'occlusion
                BlockPos wallBase = new BlockPos(
                    (int) Math.floor(localPos[0]),
                    (int) Math.floor(localPos[1]),
                    (int) Math.floor(localPos[2]) + 1
                );
                BlockState stone = Blocks.STONE.defaultBlockState();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        world.setBlockAndUpdate(wallBase.offset(dx, dy, 0), stone);
                    }
                }

                GameProfile profile = new GameProfile(UUID_CREATIVE, "WallCreative");
                CommonListenerCookie data = CommonListenerCookie.createInitial(profile, false);
                ServerPlayer mock = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
                Connection connection = new Connection(PacketFlow.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerList().placeNewPlayer(connection, mock, data);
                mock.setPos(localPos[0], localPos[1], localPos[2] + 4.0);
                mock.setYRot(180.0f);
                mock.setXRot(0.0f);
            });

            // Passer en Creative via le serveur pour que le HUD et les abilities soient corrects
            // Les mock players ont des UUIDs cafebabe-*, le joueur local a un UUID différent
            sp.getServer().runOnServer(server ->
                server.getPlayerList().getPlayers().stream()
                    .filter(p -> !p.getStringUUID().startsWith("cafebabe"))
                    .findFirst()
                    .ifPresent(p -> p.setGameMode(GameType.CREATIVE))
            );
            // Attendre que le mode Creative soit effectivement appliqué côté client
            context.waitFor(mc -> mc.player.getAbilities().instabuild);

            context.runOnClient(mc -> RpNameClientCache.set(UUID_CREATIVE, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.gui.getChat().clearMessages(false));
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
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.player.setYRot(0.0f);
                mc.player.setXRot(-20.0f);
            });

            sp.getServer().runOnServer(server -> {
                var world = server.overworld();

                world.setDayTime(6000L);
                world.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
                world.getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
                world.resetWeatherCycle();

                // Mur de stone entre la caméra et le mock player (z+1), 3 blocs de large, 4 de haut
                BlockPos wallBase = new BlockPos(
                    (int) Math.floor(localPos[0]),
                    (int) Math.floor(localPos[1]),
                    (int) Math.floor(localPos[2]) + 1
                );
                BlockState stone = Blocks.STONE.defaultBlockState();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        world.setBlockAndUpdate(wallBase.offset(dx, dy, 0), stone);
                    }
                }

                // Mock player derrière le mur à z+4
                GameProfile profile = new GameProfile(UUID_OCCLUDED, "BehindWall");
                CommonListenerCookie data = CommonListenerCookie.createInitial(profile, false);
                ServerPlayer mock = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
                Connection connection = new Connection(PacketFlow.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerList().placeNewPlayer(connection, mock, data);
                mock.setPos(localPos[0], localPos[1], localPos[2] + 4.0);
                mock.setYRot(180.0f);
                mock.setXRot(0.0f);
            });

            context.waitTicks(20);
            // Activer un pseudo RP pour s'assurer que le nametag serait visible sans occlusion
            context.runOnClient(mc -> RpNameClientCache.set(UUID_OCCLUDED, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.gui.getChat().clearMessages(false));
            context.waitTicks(2);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("nameplate_occluded_by_wall")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }
}
