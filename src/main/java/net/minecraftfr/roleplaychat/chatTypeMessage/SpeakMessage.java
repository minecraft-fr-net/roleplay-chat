package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;

public class SpeakMessage extends MessageType {
  public static final int RADIUS = 30;
  public static final int COLOR = 0xFFFFFF;
  public static final String CHARACTER = null;

  public SpeakMessage(String message) {
    super(message, RADIUS, COLOR, CHARACTER);
  }

  @Override
  public boolean canBeSend() {
    ShoutMessage shoutMessage = new ShoutMessage(message);
    WhisperMessage whisperMessage = new WhisperMessage(message);
    ActionMessage actionMessage = new ActionMessage(message);
    return (
      !shoutMessage.canBeSend() &&
      !whisperMessage.canBeSend() &&
      !actionMessage.canBeSend()
    );
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player) {
    return getChatName(player) + " " + message;
  }
}
