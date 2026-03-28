package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class ShoutMessage extends MessageType {
  public static final String COMMAND = "shout";

  public ShoutMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), settings.firstPrefix());
    this.prefixLength = settings.prefixLengthFor(message);
  }
}
