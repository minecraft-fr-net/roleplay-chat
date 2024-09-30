package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class ShoutMessage {
  public static final int RADIUS = 80;
  public static final int COLOR = 0xCC3300;
  public static final String CHARACTER = "!";

  public static boolean canBeSend(String message) {
    return message.startsWith(CHARACTER);
  }

  public static MutableText formatMessage(ServerPlayerEntity player, String message) {
    String contentMessage = formatContentMessage(player, message);
    return Text.literal(contentMessage).styled(style -> 
      style.withColor(TextColor.fromRgb(COLOR))
    );
  }

  public static String formatContentMessage(ServerPlayerEntity player, String message) {
    return "<"+player.getName().getString() + "> " + message.substring(1).trim();
  }
}
