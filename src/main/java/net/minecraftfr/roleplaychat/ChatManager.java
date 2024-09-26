package net.minecraftfr.roleplaychat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatManager {

  // Constants for message ranges and formatting
  public static final int SPEAK_RADIUS = 30;
  public static final Formatting SPEAK_COLOR = Formatting.WHITE;

  // Handle the "speak" message (no trigger character)
  public void handleSpeakMessage(ServerPlayerEntity player, String message) {
    sendLocalMessage(player, message);
  }

  // Send message to players within a certain radius
  private void sendLocalMessage(ServerPlayerEntity player, String message) {
    player.getServerWorld().getPlayers().forEach(otherPlayer -> {
      if (player.getPos().isInRange(otherPlayer.getPos(), SPEAK_RADIUS)) {
        // Send the message in the specified color
        otherPlayer.sendMessage(
          Text.literal(player.getName().getString() + ": " + message).formatted(SPEAK_COLOR),
          false
        );
      }
    });
  }
}
