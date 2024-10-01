package net.minecraftfr.roleplaychat.chatTypeMessage;

public class ShoutMessage extends MessageType {
  public static final int RADIUS = 80;
  public static final int COLOR = 0xCC3300;
  public static final String CHARACTER = "!";

  public ShoutMessage(String message) {
    super(message, RADIUS, COLOR, CHARACTER);
  }
}
