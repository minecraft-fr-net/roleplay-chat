package net.minecraftfr.roleplaychat.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/**
 * Test client qui vérifie visuellement l'affichage d'un message Speak via le vrai flow de chat.
 *
 * <p>Le joueur envoie un message sans préfixe → {@code ChatManager} le traite comme un
 * {@code SpeakMessage} → {@code MessageType.sendMessage} est appelé avec un vrai
 * {@code SignedMessage} → {@code SentMessage.Chat} → aucun indicateur système côté client.
 *
 * <p>Au premier lancement, le template de référence est auto-généré dans
 * {@code src/client/resources/templates/speak_radius_display.png}.
 * Validez-le manuellement, puis les runs suivants feront une assertion de comparaison.
 *
 * <p><strong>Note</strong> : le filtrage par rayon (radius) est couvert par les tests serveur
 * {@code ChatManagerRadiusTest}. Ce test vérifie uniquement que l'affichage ne montre pas
 * d'indicateur système quand le message passe par le vrai flow.
 */
@SuppressWarnings("UnstableApiUsage")
public class ChatRadiusDisplayTest implements FabricClientGameTest {

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

            // Speak via le vrai flow de chat : sendChatMessage → ChatManager → SpeakMessage
            // → MessageType.sendMessage(sender, receiver, signedMessage) avec un vrai SignedMessage
            // → SentMessage.Chat → indicateur SECURE (aucun indicateur) en mode intégré
            context.runOnClient(client -> {
                var handler = client.getNetworkHandler();
                if (handler != null) {
                    handler.sendChatMessage("Vous m'entendez ?");
                }
            });

            // Attendre que le message soit traité et affiché dans le HUD
            context.waitTicks(20);

            // Comparaison contre le template de référence (auto-créé au premier run)
            context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("speak_radius_display")
                .withAlgorithm(TestScreenshotComparisonAlgorithm.meanSquaredDifference(0.02f)));
        }
    }
}
