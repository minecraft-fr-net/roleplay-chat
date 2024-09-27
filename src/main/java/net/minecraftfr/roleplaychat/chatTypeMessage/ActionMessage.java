package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.util.Formatting;

public class ActionMessage {
  public static final int RADIUS = 25;
  public static final Formatting COLOR = Formatting.GREEN;
  public static final String CHARACTER = "*";

	public static boolean isDoingAnAction(String message) {
		return message.startsWith(CHARACTER);
	}
}
