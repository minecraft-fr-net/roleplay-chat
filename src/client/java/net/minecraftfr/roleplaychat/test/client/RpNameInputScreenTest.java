package net.minecraftfr.roleplaychat.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraftfr.roleplaychat.screen.RpNameInputScreen;

@SuppressWarnings("UnstableApiUsage")
public class RpNameInputScreenTest implements FabricClientGameTest {

  private static final int VIEWPORT_WIDTH = 854;
  private static final int VIEWPORT_HEIGHT = 480;

  @Override
  public void runTest(ClientGameTestContext context) {
    context.getInput().resizeWindow(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

    try (TestSingleplayerContext sp = context.worldBuilder().create()) {
      sp.getClientWorld().waitForChunksDownload();

      // Cas 1 — écran vide (premier lancement)
      context.runOnClient(client -> client.setScreen(new RpNameInputScreen("", "")));
      context.waitTicks(5);
      context.assertScreenshotEquals("rp_name_input_screen_empty");

      context.runOnClient(client -> client.setScreen(null));
      context.waitTicks(2);

      // Cas 2 — écran avec message d'erreur (nom déjà pris)
      context.runOnClient(client ->
          client.setScreen(new RpNameInputScreen("screen.roleplay-chat.rp_name_input.error.taken", "Elara")));
      context.waitTicks(5);
      context.assertScreenshotEquals("rp_name_input_screen_error");
    }
  }
}
