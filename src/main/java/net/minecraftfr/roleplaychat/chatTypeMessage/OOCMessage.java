package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;

public class OOCMessage extends MessageType {
  public static final int RADIUS = 60;
  public static final int COLOR = 0xAEC1D5;
  public static final String CHARACTER = "(";
  
  public OOCMessage() {
    super(RADIUS, COLOR, CHARACTER);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player, String message) {
    return getChatName(player) + " ( " + message.substring(1).trim() + " )";
  }
}
