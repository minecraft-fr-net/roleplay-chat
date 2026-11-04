package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;

public class SpeakMessage extends MessageType {
  public static final String COMMAND = "speak";

  public SpeakMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), null);
    this.prefixLength = 0;
  }

  @Override
  public boolean canBeSend() {
    return RoleplayChatConfig.get().wouldBeSpeakExcludingShoutWhisperAction(message);
  }

  @Override
  public String formatContentMessage(ServerPlayer player) {
    return getChatName(player) + " " + message;
  }
}
