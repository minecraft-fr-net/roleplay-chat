package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class WhisperMessage extends MessageType {
  public static final String COMMAND = "whisper";

  private final MessageTypeSettings settings;

  public WhisperMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), null);
    this.settings = settings;
    this.prefixLength = settings.prefixLengthFor(message);
  }

  @Override
  public boolean canBeSend() {
    return settings.messageMatchesPrefix(message);
  }
}
