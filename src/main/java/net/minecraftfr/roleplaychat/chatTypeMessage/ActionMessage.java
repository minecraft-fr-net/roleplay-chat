package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;

public class ActionMessage extends MessageType {
  public static final int RADIUS = 25;
  public static final int COLOR = 0x33CC33;
  public static final String CHARACTER = "*";

  public ActionMessage() {
    super(RADIUS, COLOR, CHARACTER);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player, String message) {
    return "* "+player.getName().getString() + " " + message.substring(1).trim();
  }
}
