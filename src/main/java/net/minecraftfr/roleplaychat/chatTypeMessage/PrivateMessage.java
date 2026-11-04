package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.ChatFormatting;

public class PrivateMessage {
  public static final int RADIUS = 0;
  public static final ChatFormatting COLOR = ChatFormatting.WHITE;
  public static final String CHARACTER = null;
  public static final String COMMAND = "m";

  public static boolean canBeSend(String message) {
    return true;
  }
}
