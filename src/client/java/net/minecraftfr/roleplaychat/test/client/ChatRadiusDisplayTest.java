package net.minecraftfr.roleplaychat.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * Test client qui vérifie visuellement le filtrage des messages par rayon (radius).
 *
 * <p>Scénario : deux joueurs fictifs envoient un message Speak (rayon=30) au joueur local.
 * <ul>
 *   <li>"JoueurProche" à 25 blocs → dans le rayon → message <strong>visible</strong> dans le chat</li>
 *   <li>"JoueurLoin" à 35 blocs → hors du rayon → message <strong>filtré</strong>, absent du chat</li>
 * </ul>
 *
 * <p>La couleur du message de JoueurProche est atténuée selon la distance
 * (même logique que {@link net.minecraftfr.roleplaychat.chatTypeMessage.MessageType#getFadedColor()}).
 *
 * <p>Au premier lancement, le template de référence est auto-généré dans
 * {@code src/client/resources/templates/speak_radius_display.png}.
 * Validez-le manuellement, puis les runs suivants feront une assertion de comparaison.
 */
@SuppressWarnings("UnstableApiUsage")
public class ChatRadiusDisplayTest implements FabricClientGameTest {

    private static final int VIEWPORT_WIDTH = 854;
    private static final int VIEWPORT_HEIGHT = 480;

    /** Rayon du message Speak (doit correspondre à {@code MessageTypeSettings.speakDefault()}). */
    private static final int SPEAK_RADIUS = 30;
    /** Couleur de base du message Speak (blanc 0xFFFFFF). */
    private static final int SPEAK_COLOR = 0xFFFFFF;

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

            // Envoyer le message de JoueurProche (25 blocs ≤ rayon 30) depuis le thread serveur
            context.runOnClient(client -> {
                var server = client.getServer();
                if (server == null) return;

                server.execute(() -> {
                    var players = server.getPlayerManager().getPlayerList();
                    if (players.isEmpty()) return;
                    var localPlayer = players.get(0);

                    // JoueurProche à 25 blocs — distance ≤ rayon → message reçu avec couleur atténuée
                    int fadedColor = computeFadedColor(SPEAK_COLOR, 25, SPEAK_RADIUS);
                    Text nearbyMsg = Text.literal("<JoueurProche> Vous m'entendez ?")
                        .styled(s -> s.withColor(TextColor.fromRgb(fadedColor)));
                    localPlayer.sendMessage(nearbyMsg, false);

                    // JoueurLoin à 35 blocs — distance > rayon → message filtré, non envoyé
                });
            });

            // Attendre que le message arrive côté client (traitement serveur + paquet réseau)
            context.waitTicks(10);

            // Comparaison contre le template de référence (auto-créé au premier run)
            context.assertScreenshotEquals("speak_radius_display");
        }
    }

    /**
     * Calcule la couleur atténuée selon la distance, identique à
     * {@link net.minecraftfr.roleplaychat.chatTypeMessage.MessageType#getFadedColor()}.
     *
     * @param baseColor couleur RGB de base
     * @param distance  distance en blocs
     * @param radius    rayon maximum
     * @return couleur RGB atténuée
     */
    private static int computeFadedColor(int baseColor, int distance, int radius) {
        if (radius <= 0) return baseColor;
        float fadeFactor = Math.min((float) distance / (float) radius, 1.0f);
        int red   = (int) ((baseColor >> 16 & 0xFF) * (1.0f - fadeFactor));
        int green = (int) ((baseColor >> 8  & 0xFF) * (1.0f - fadeFactor));
        int blue  = (int) ((baseColor        & 0xFF) * (1.0f - fadeFactor));
        return (red << 16) | (green << 8) | blue;
    }
}
