package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.util.Formatting;

public class WhisperMessage {
  public static final int RADIUS = 4;
  public static final Formatting COLOR = Formatting.DARK_PURPLE;
  public static final String CHARACTER = "«";

  public static boolean canBeSend(String message) {
		return message.startsWith(CHARACTER);
	}
}
