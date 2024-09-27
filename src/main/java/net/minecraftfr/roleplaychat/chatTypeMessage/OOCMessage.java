package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.util.Formatting;

public class OOCMessage {
  public static final int RADIUS = 60;
  public static final Formatting COLOR = Formatting.GRAY;
  public static final String CHARACTER = "(";

  public static boolean canBeSend(String message) {
    return message.startsWith(CHARACTER);
  }
}
