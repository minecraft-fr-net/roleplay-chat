package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class ActionMessage extends MessageType {
  public static final String COMMAND = "action";

  public ActionMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), settings.firstPrefix());
    this.prefixLength = settings.prefixLengthFor(message);
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player) {
    return "* " + player.getName().getString() + " " + contentAfterPrefix();
  }
}
