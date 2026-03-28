package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class GlobalOOCMessage extends MessageType {
  public static final String COMMAND = "globalOoc";

  public GlobalOOCMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), settings.firstPrefix());
    this.prefixLength = settings.prefixLengthFor(message);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player) {
    return getChatName(player) + " [ " + contentAfterPrefix() + " ]";
  }
}
