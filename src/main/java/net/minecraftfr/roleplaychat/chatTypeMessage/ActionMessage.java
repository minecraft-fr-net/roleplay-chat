package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class ActionMessage extends MessageType {
  public static final int RADIUS = 25;
  public static final int COLOR = 0x33CC33;
  public static final String CHARACTER = "*";

  public ActionMessage() {
    super(RADIUS);
  }

  @Override
  public boolean canBeSend(String message) {
    return message.startsWith(CHARACTER);
  }

  @Override
  public MutableText formatMessage(ServerPlayerEntity player, String message) {
    String contentMessage = formatContentMessage(player, message);
    return Text.literal(contentMessage).styled(style -> 
      style.withColor(TextColor.fromRgb(COLOR))
    );
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player, String message) {
    return "* "+player.getName().getString() + " " + message.substring(1).trim();
  }
}
