package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.util.Formatting;

public class ShoutMessage {
  public static final int RADIUS = 80;
  public static final Formatting COLOR = Formatting.RED;
  public static final String CHARACTER = "!";

  public static boolean canBeSend(String message) {
		return message.startsWith(CHARACTER);
	}
}
