package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;

public class OOCMessage extends MessageType {
  public static final int RADIUS = 60;
  public static final int COLOR = 0xAEC1D5;
  public static final String CHARACTER = "(";
  
  public OOCMessage(String message) {
    super(message, RADIUS, COLOR, CHARACTER);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player) {
    return getChatName(player) + " ( " + message.substring(1).trim() + " )";
  }
}
