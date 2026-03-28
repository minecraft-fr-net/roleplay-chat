package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public abstract class MessageType {
  protected String message;
  protected int radius;
  protected int color;
  protected String character;
  protected String command;
  protected Integer distance;
  /** Longueur du préfixe retiré dans {@link #contentAfterPrefix()} (0 = message entier). */
  protected int prefixLength = 1;

  public MessageType(String message, int radius, int color, String character) {
    this.message = message;
    this.radius = radius;
    this.color = color;
    this.character = character;
  }

  protected String contentAfterPrefix() {
    if (prefixLength <= 0) {
      return message.trim();
    }
    if (prefixLength >= message.length()) {
      return "";
    }
    return message.substring(prefixLength).trim();
  }

  public int getRadius() {
    return radius;
  }

  public String getMessage() {
    return message;
  }

  public boolean canBeSend() {
    return character != null && message.startsWith(character);
  }

  public MutableText formatMessage(ServerPlayerEntity player) {
    String contentMessage = formatContentMessage(player);
    return Text.literal(contentMessage).styled(style -> 
      style.withColor(TextColor.fromRgb(getFadedColor()))
    );
  }

  public String formatContentMessage(ServerPlayerEntity player) {
    return getChatName(player) + " " + contentAfterPrefix();
  }

  public void sendMessage(ServerPlayerEntity sender, ServerPlayerEntity receiver) {
    receiver.sendMessage(
      this.formatMessage(sender),
      false
    );
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

  /*
   * Return the name of the player for the chat
   * Example : <Jeb_>
   */
  protected String getChatName(ServerPlayerEntity player) {
    return "<" + player.getName().getString() + ">";
  }

  protected int getDistance() {
    return distance;
  }

  /*
   * Return the color for the message with the opacity
   * The opacity is defined from the distance between the two players
   */
  protected int getFadedColor() {
    if (distance == null) {
      return this.color;
    }

    int maxDistance = radius;
    if (maxDistance <= 0) {
      return this.color;
    }

    // Calcul du facteur de fade entre 0 (proche) et 1 (loin)
    float fadeFactor = Math.min((float)distance / (float)maxDistance, 1.0f);

    // Calcul des nouvelles composantes RGB basées sur le fadeFactor
    int red = (int) ((color >> 16 & 0xFF) * (1.0 - fadeFactor));
    int green = (int) ((color >> 8 & 0xFF) * (1.0 - fadeFactor));
    int blue = (int) ((color & 0xFF) * (1.0 - fadeFactor));

    // Combiner les composantes pour obtenir la nouvelle couleur
    int fadedColor = (red << 16) | (green << 8) | blue;

    return fadedColor;
  }
}
