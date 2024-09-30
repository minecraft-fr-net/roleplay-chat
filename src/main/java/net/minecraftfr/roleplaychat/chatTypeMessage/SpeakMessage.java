package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpeakMessage {
  public static final int RADIUS = 30;
  public static final Formatting COLOR = Formatting.WHITE;
  public static final String CHARACTER = null;

  public static boolean canBeSend(String message) {
    return (
      !ShoutMessage.canBeSend(message) &&
      !WhisperMessage.canBeSend(message) &&
      !ActionMessage.canBeSend(message)
    );
  }

  public static MutableText formatMessage(ServerPlayerEntity player, String message) {
    String contentMessage = formatContentMessage(player, message);
    return Text.literal(contentMessage).formatted(COLOR);
  }

  public static String formatContentMessage(ServerPlayerEntity player, String message) {
    return "<"+player.getName().getString() + "> " + message;
  }
}
