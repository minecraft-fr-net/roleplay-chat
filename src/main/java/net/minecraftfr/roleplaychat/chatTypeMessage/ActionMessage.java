package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class ActionMessage extends MessageType {
  public static final String COMMAND = "action";

  public ActionMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), settings.firstPrefix());
    this.prefixLength = settings.prefixLengthFor(message);
  }

  /** Format action : {@code * NomJoueur message} (sans chevrons). */
  @Override
  protected String getChatName(ServerPlayer player) {
    return "* " + getPlayerDisplayName(player);
  }
}
