package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpeakMessage extends MessageType {
  public static final int RADIUS = 30;
  public static final Formatting COLOR = Formatting.WHITE;
  public static final String CHARACTER = null;

  public SpeakMessage() {
    super(RADIUS);
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
  public MutableText formatMessage(ServerPlayerEntity player, String message) {
    String contentMessage = formatContentMessage(player, message);
    return Text.literal(contentMessage).formatted(COLOR);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player, String message) {
    return "<"+player.getName().getString() + "> " + message;
  }
}
