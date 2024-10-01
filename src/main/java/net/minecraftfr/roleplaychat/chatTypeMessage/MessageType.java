package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public abstract class MessageType {
  protected int radius;
  protected int color;
  protected String character;

  public MessageType(int radius, int color, String character) {
    this.radius = radius;
    this.color = color;
    this.character = character;
  }

  public int getRadius() {
    return radius;
  }

  public boolean canBeSend(String message) {
    return message.startsWith(character);
  }

  public MutableText formatMessage(ServerPlayerEntity player, String message) {
    String contentMessage = formatContentMessage(player, message);
    return Text.literal(contentMessage).styled(style -> 
      style.withColor(TextColor.fromRgb(color))
    );
  }

  public String formatContentMessage(ServerPlayerEntity player, String message) {
    return getChatName(player) + " " + message.substring(1).trim();
  }

  protected String getChatName(ServerPlayerEntity player) {
    return "<" + player.getName().getString() + ">";
  }
}
