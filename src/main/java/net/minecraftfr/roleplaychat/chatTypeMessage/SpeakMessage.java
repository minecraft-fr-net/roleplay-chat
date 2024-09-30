package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;

public class SpeakMessage extends MessageType {
  public static final int RADIUS = 30;
  public static final int COLOR = 0xFFFFFF;
  public static final String CHARACTER = null;

  public SpeakMessage() {
    super(RADIUS, COLOR, CHARACTER);
  }

  @Override
  public boolean canBeSend(String message) {
    ShoutMessage shoutMessage = new ShoutMessage();
    WhisperMessage whisperMessage = new WhisperMessage();
    ActionMessage actionMessage = new ActionMessage();
    return (
      !shoutMessage.canBeSend(message) &&
      !whisperMessage.canBeSend(message) &&
      !actionMessage.canBeSend(message)
    );
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player, String message) {
    return "<"+player.getName().getString() + "> " + message;
  }
}
