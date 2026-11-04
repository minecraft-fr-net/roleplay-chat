package net.minecraftfr.roleplaychat.test.client;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.minecraft.client.CameraType;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealedCache;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NameplateDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;

    // UUIDs fixes pour skins déterministes entre les runs (Steve par défaut car hashCode pair)
    private static final UUID UUID_WITH_RP    = UUID.fromString("cafebabe-0000-0000-0000-000000000001");
    private static final UUID UUID_WITHOUT_RP = UUID.fromString("cafebabe-0000-0000-0000-000000000002");
    private static final UUID UUID_UNKNOWN    = UUID.fromString("cafebabe-0000-0000-0000-000000000003");

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        testNameplateWithRpName(context);
        testNameplateWithoutRpName(context);
        testNameplateUnknownPlayer(context);
    }

    private static void testNameplateWithRpName(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            spawnMockPlayerInFront(sp, context, "MockPlayer", UUID_WITH_RP);
            // Attendre que tous les paquets JOIN soient traités, puis écraser avec des valeurs fixes
            context.runOnClient(mc -> {
                PlayerCodeClientCache.set(UUID_WITH_RP, "#AAAAAA");
                RpNameClientCache.set(UUID_WITH_RP, "Elara");
                RpNameRevealedCache.add(UUID_WITH_RP);
            });
            takeCleanScreenshot(context, "nameplate_with_rp_name");
        }
    }

    private static void testNameplateWithoutRpName(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            spawnMockPlayerInFront(sp, context, "MockPlayer", UUID_WITHOUT_RP);
            // Code connu mais pas de nom RP → affiche "?" + code
            context.runOnClient(mc -> {
                PlayerCodeClientCache.set(UUID_WITHOUT_RP, "#CCCCCC");
                RpNameClientCache.clear();
            });
            takeCleanScreenshot(context, "nameplate_without_rp_name");
        }
    }

    private static void testNameplateUnknownPlayer(ClientGameTestContext context) {
        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            spawnMockPlayerInFront(sp, context, "UnknownPlayer", UUID_UNKNOWN);
            // Code connu + nom RP connu mais pas révélé → affiche "?" + code
            context.runOnClient(mc -> {
                PlayerCodeClientCache.set(UUID_UNKNOWN, "#BBBBBB");
                RpNameClientCache.set(UUID_UNKNOWN, "Elrich");
                // Pas de RpNameRevealedCache.add() → le nom RP reste masqué
            });
            takeCleanScreenshot(context, "nameplate_unknown_player");
        }
    }

    private static void takeCleanScreenshot(ClientGameTestContext context, String name) {
        // Vider le chat pour éviter le message "MockPlayer joined the game"
        context.runOnClient(mc -> mc.gui.getChat().clearMessages(false));
        context.waitTicks(2);
        context.assertScreenshotEquals(
            TestScreenshotComparisonOptions.of(name)
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f))
        );
    }

    private static void spawnMockPlayerInFront(TestSingleplayerContext sp, ClientGameTestContext context, String name, UUID uuid) {
        context.waitFor(mc -> mc.player != null && mc.player.getY() != 0);

        double[] localPos = new double[3];
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

            GameProfile profile = new GameProfile(uuid, name);
            CommonListenerCookie data = CommonListenerCookie.createInitial(profile, false);
            ServerPlayer mock = new ServerPlayer(server, world, profile, ClientInformation.createDefault());

            Connection connection = new Connection(PacketFlow.SERVERBOUND);
            new EmbeddedChannel(connection);
            server.getPlayerList().placeNewPlayer(connection, mock, data);

            // 3 blocs devant le joueur local (direction sud +Z), face à lui
            mock.setPos(localPos[0], localPos[1], localPos[2] + 3.0);
            mock.setYRot(180.0f);
            mock.setXRot(0.0f);
        });

        context.waitTicks(20);
    }
}
