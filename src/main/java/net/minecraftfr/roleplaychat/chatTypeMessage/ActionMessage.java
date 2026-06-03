package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
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

  /**
   * Format action : {@code * NomRP action message}
   * Hover sur le nom → MC username si un pseudo RP est défini.
   */
  @Override
  public MutableText formatMessage(ServerPlayerEntity player) {
    int fadedColor = getFadedColor();
    TextColor textColor = TextColor.fromRgb(fadedColor);

    // Nom à afficher : pseudo RP si défini, sinon MC name
    net.minecraft.server.MinecraftServer server = player.getServer();
    String displayName = player.getName().getString();
    String rpName = null;
    if (server != null) {
      rpName = net.minecraftfr.roleplaychat.nameplate.RpNameStore
          .get(server).getRpName(player.getUuid()).orElse(null);
      if (rpName != null) displayName = rpName;
    }

    // Segment "* NomJoueur" avec hover MC username si pseudo RP défini
    MutableText nameSegment = Text.literal("* " + displayName)
        .styled(style -> style.withColor(textColor));

    if (rpName != null) {
      String mcName = player.getName().getString();
      nameSegment.styled(style ->
          style.withHoverEvent(new net.minecraft.text.HoverEvent.ShowText(
              Text.literal(mcName))));
    }

    // Segment contenu
    MutableText contentSegment = Text.literal(" " + contentAfterPrefix())
        .styled(style -> style.withColor(textColor));

    return nameSegment.append(contentSegment);
  }
}
