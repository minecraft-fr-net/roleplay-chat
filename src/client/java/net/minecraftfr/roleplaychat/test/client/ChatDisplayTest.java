package net.minecraftfr.roleplaychat.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/**
 * Test client qui envoie des messages de chat de chaque type
 * et capture un screenshot pour vérifier l'affichage dans le chat.
 */
@SuppressWarnings("UnstableApiUsage")
public class ChatDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        try (TestSingleplayerContext sp = context.worldBuilder().create()) {
            sp.getClientWorld().waitForChunksDownload();
            sp.getClientWorld().waitForChunksRender();

            // Vue à la première personne pour voir le chat sans obstruction
            context.runOnClient((MinecraftClient client) ->
                client.options.setPerspective(Perspective.FIRST_PERSON)
            );
            context.waitTicks(20);

            // Speak — message normal sans préfixe
            sendAndCapture(context, "Bonjour à tous !", "chat_speak");

            // Shout — préfixe '!'
            sendAndCapture(context, "!Cri de guerre !", "chat_shout");

            // Action — préfixe '*'
            sendAndCapture(context, "*court vers la porte", "chat_action");

            // Whisper — préfixe '«'
            sendAndCapture(context, "«Psst, tu m'entends ?", "chat_whisper");

            // OOC — préfixe '('
            sendAndCapture(context, "(je reviens dans 5 min)", "chat_ooc");

            // GlobalOOC — préfixe '['
            sendAndCapture(context, "[annonce serveur]", "chat_global_ooc");
        }
    }

    /**
     * Envoie un message de chat et capture un screenshot après 20 ticks
     * (le temps que le message s'affiche dans le HUD).
     */
    private static void sendAndCapture(ClientGameTestContext context, String message, String screenshotName) {
        context.runOnClient((MinecraftClient client) -> {
            var handler = client.getNetworkHandler();
            if (handler != null) {
                handler.sendChatMessage(message);
            }
        });
        context.waitTicks(20);
        context.takeScreenshot(screenshotName);
    }
}
