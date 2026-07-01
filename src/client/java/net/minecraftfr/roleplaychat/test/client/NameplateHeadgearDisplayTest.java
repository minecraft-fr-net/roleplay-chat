package net.minecraftfr.roleplaychat.test.client;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
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

                GameProfile profile = new GameProfile(UUID_CREATIVE, "HoodCreative");
                ConnectedClientData data = ConnectedClientData.createDefault(profile, false);
                ServerPlayerEntity mock = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
                ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerManager().onPlayerConnect(connection, mock, data);

                mock.setPos(localPos[0], localPos[1], localPos[2] + 3.0);
                mock.setYaw(180.0f);
                mock.setPitch(0.0f);
            });

            context.waitTicks(20);

            context.runOnClient(mc -> {
                var mockPlayer = mc.world.getPlayerByUuid(UUID_CREATIVE);
                if (mockPlayer != null) {
                    mockPlayer.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                }
            });

            // Passer en Creative — pattern établi dans NameplateOcclusionDisplayTest
            sp.getServer().runOnServer(server ->
                server.getPlayerManager().getPlayerList().stream()
                    .filter(p -> !p.getUuidAsString().startsWith("cafebabe"))
                    .findFirst()
                    .ifPresent(p -> p.changeGameMode(GameMode.CREATIVE))
            );
            context.waitFor(mc -> mc.player.getAbilities().creativeMode);

            context.runOnClient(mc -> RpNameClientCache.set(UUID_CREATIVE, "Elara"));
            context.waitTicks(5);

            context.runOnClient(mc -> mc.inGameHud.getChatHud().clear(false));
            context.waitTicks(2);
            context.assertScreenshotEquals("nameplate_headgear_visible_in_creative");
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

                GameProfile profile = new GameProfile(UUID_HEADGEAR, "HoodedPlayer");
                ConnectedClientData data = ConnectedClientData.createDefault(profile, false);
                ServerPlayerEntity mock = new ServerPlayerEntity(server, world, profile, SyncedClientOptions.createDefault());
                ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
                new EmbeddedChannel(connection);
                server.getPlayerManager().onPlayerConnect(connection, mock, data);

                mock.setPos(localPos[0], localPos[1], localPos[2] + 3.0);
                mock.setYaw(180.0f);
                mock.setPitch(0.0f);
                // Équiper un leather_helmet (présent dans le tag conceals_identity)
                mock.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            });

            context.waitTicks(20);

            // L'équipement du mock player ne se propage pas via EmbeddedChannel —
            // on le force directement sur l'entité côté client
            context.runOnClient(mc -> {
                var mockPlayer = mc.world.getPlayerByUuid(UUID_HEADGEAR);
                if (mockPlayer != null) {
                    mockPlayer.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                }
            });

            // Simuler une présentation préalable : le joueur s'est présenté, mais la cagoule
            // doit quand même cacher son nom RP et ne montrer que "?" + code hex.
            context.runOnClient(mc -> {
                PlayerCodeClientCache.set(UUID_HEADGEAR, "#DDDDDD");
                RpNameClientCache.set(UUID_HEADGEAR, "Elara");
                RpNameRevealedCache.add(UUID_HEADGEAR);
            });

            context.runOnClient(mc -> mc.inGameHud.getChatHud().clear(false));
            context.waitTicks(2);
            context.assertScreenshotEquals("nameplate_hidden_by_headgear");
        }
    }
}
