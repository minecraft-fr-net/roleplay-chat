package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class WhisperMessage {
  public static final int RADIUS = 4;
  public static final int COLOR = 0xCC33CC;
  public static final String[] CHARACTERS = {"«", "\""};

  public static boolean canBeSend(String message) {
    for (String character : CHARACTERS) {
      if (message.startsWith(character)) {
        return true;
      }
    }
    return false;
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
