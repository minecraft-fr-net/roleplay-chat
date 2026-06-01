package net.minecraftfr.roleplaychat.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/**
 * Test visuel du système de pseudo RP (/rpname) :
 * vérifie que le chat affiche {@code <Elara> Bonjour à tous !}
 * après qu'un joueur a défini son pseudo via {@code /rpname Elara}.
 */
@SuppressWarnings("UnstableApiUsage")
public class RpNameDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            sp.getClientWorld().waitForChunksDownload();
            sp.getClientWorld().waitForChunksRender();

            context.runOnClient((MinecraftClient client) ->
                client.options.setPerspective(Perspective.FIRST_PERSON)
            );
            context.waitTicks(20);

            // Définir le pseudo RP via commande envoyée par le joueur client
            context.runOnClient(client -> {
                var handler = client.getNetworkHandler();
                if (handler != null) {
                    handler.sendChatCommand("rpname Elara");
                }
            });
            context.waitTicks(20);

            // Envoyer un message : doit afficher "<Elara> Bonjour à tous !"
            context.runOnClient(client -> {
                var handler = client.getNetworkHandler();
                if (handler != null) {
                    handler.sendChatMessage("Bonjour à tous !");
                }
            });
            context.waitTicks(20);

            context.assertScreenshotEquals("rpname_chat");
        }
    }
}
