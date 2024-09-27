package net.minecraftfr.roleplaychat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;

public class ChatManager {
  public void handleChatMessage(ServerPlayerEntity player, String message) {
    if (SpeakMessage.canBeSend(message)) {
      this.sendLocalMessage(player, message, SpeakMessage.RADIUS, SpeakMessage.COLOR);
    }
  }

  /**
   * Send message to players within a certain radius in the specified color
   * Send to all players if radius is 0
   */
  private void sendLocalMessage(ServerPlayerEntity player, String message, int radius, Formatting color) {
    player.getServerWorld().getPlayers().forEach(otherPlayer -> {
      if (radius == 0 || player.getPos().isInRange(otherPlayer.getPos(), radius)) {
        otherPlayer.sendMessage(
          Text.literal(player.getName().getString() + ": " + message).formatted(color),
          false
        );
      }
    });
  }
}
