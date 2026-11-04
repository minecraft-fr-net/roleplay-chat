package net.minecraftfr.roleplaychat.test.client;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealedCache;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NameplateHeadgearDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;
    private static final UUID UUID_HEADGEAR  = UUID.fromString("cafebabe-0000-0000-0000-000000000006");
    private static final UUID UUID_CREATIVE  = UUID.fromString("cafebabe-0000-0000-0000-000000000007");

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        testNameplateHiddenByHeadgear(context);
        testNameplateVisibleThroughHeadgearInCreative(context);
    }

    private static void testNameplateVisibleThroughHeadgearInCreative(ClientGameTestContext context) {
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

                GameProfile profile = new GameProfile(UUID_CREATIVE, "HoodCreative");
                CommonListenerCookie data = CommonListenerCookie.createInitial(profile, false);
                ServerPlayer mock = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
                Connection connection = new Connection(PacketFlow.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerList().placeNewPlayer(connection, mock, data);

                mock.setPos(localPos[0], localPos[1], localPos[2] + 3.0);
                mock.setYRot(180.0f);
                mock.setXRot(0.0f);
            });

            context.waitTicks(20);

            context.runOnClient(mc -> {
                var mockPlayer = mc.level.getPlayerByUUID(UUID_CREATIVE);
                if (mockPlayer != null) {
                    mockPlayer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                }
            });

            // Passer en Creative — pattern établi dans NameplateOcclusionDisplayTest
            sp.getServer().runOnServer(server ->
                server.getPlayerList().getPlayers().stream()
                    .filter(p -> !p.getStringUUID().startsWith("cafebabe"))
                    .findFirst()
                    .ifPresent(p -> p.setGameMode(GameType.CREATIVE))
            );
            context.waitFor(mc -> mc.player.getAbilities().instabuild);

            context.runOnClient(mc -> RpNameClientCache.set(UUID_CREATIVE, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.gui.getChat().clearMessages(false));
            context.waitTicks(2);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("nameplate_headgear_visible_in_creative")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }

    private static void testNameplateHiddenByHeadgear(ClientGameTestContext context) {
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

                GameProfile profile = new GameProfile(UUID_HEADGEAR, "HoodedPlayer");
                CommonListenerCookie data = CommonListenerCookie.createInitial(profile, false);
                ServerPlayer mock = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
                Connection connection = new Connection(PacketFlow.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerList().placeNewPlayer(connection, mock, data);

                mock.setPos(localPos[0], localPos[1], localPos[2] + 3.0);
                mock.setYRot(180.0f);
                mock.setXRot(0.0f);
                // Équiper un leather_helmet (présent dans le tag conceals_identity)
                mock.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            });

            context.waitTicks(20);

            // L'équipement du mock player ne se propage pas via EmbeddedChannel —
            // on le force directement sur l'entité côté client
            context.runOnClient(mc -> {
                var mockPlayer = mc.level.getPlayerByUUID(UUID_HEADGEAR);
                if (mockPlayer != null) {
                    mockPlayer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                }
            });

            // Simuler une présentation préalable : le joueur s'est présenté, mais la cagoule
            // doit quand même cacher son nom RP et ne montrer que "?" + code hex.
            context.runOnClient(mc -> {
                PlayerCodeClientCache.set(UUID_HEADGEAR, "#DDDDDD");
                RpNameClientCache.set(UUID_HEADGEAR, "Elara");
                RpNameRevealedCache.add(UUID_HEADGEAR);
            });

            context.runOnClient(mc -> mc.gui.getChat().clearMessages(false));
            context.waitTicks(2);
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("nameplate_hidden_by_headgear")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }
}
