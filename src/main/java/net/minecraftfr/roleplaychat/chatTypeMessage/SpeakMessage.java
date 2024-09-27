package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.util.Formatting;

public class SpeakMessage {
  public static final int RADIUS = 30;
  public static final Formatting COLOR = Formatting.WHITE;
  public static final String CHARACTER = null;

  public static boolean isSpeaking(String message) {
		return (
      !ShoutMessage.isShouting(message) &&
      !WhisperMessage.isWhispering(message) &&
      !ActionMessage.isDoingAnAction(message)
    );
	}
}
