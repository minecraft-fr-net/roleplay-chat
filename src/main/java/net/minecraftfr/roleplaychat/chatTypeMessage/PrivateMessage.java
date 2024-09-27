package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.util.Formatting;

public class PrivateMessage {
  public static final int RADIUS = 0;
  public static final Formatting COLOR = Formatting.WHITE;
  public static final String CHARACTER = "/m ";

  public static boolean canBeSend(String message) {
    return message.startsWith(CHARACTER);
  }
}
