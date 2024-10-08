package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;

public class ActionMessage extends MessageType {
  public static final int RADIUS = 25;
  public static final int COLOR = 0x33CC33;
  public static final String CHARACTER = "*";
  public static final String COMMAND = "action";

  public ActionMessage(String message) {
    super(message, RADIUS, COLOR, CHARACTER);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player) {
    return "* " + player.getName().getString() + " " + message.substring(1).trim();
  }
}
