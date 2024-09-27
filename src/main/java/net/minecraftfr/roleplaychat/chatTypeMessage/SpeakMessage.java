package net.minecraftfr.roleplaychat.chatTypeMessage;

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
}
