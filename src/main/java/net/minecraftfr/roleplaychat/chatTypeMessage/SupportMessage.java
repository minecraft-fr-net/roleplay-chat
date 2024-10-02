package net.minecraftfr.roleplaychat.chatTypeMessage;

public class SupportMessage extends MessageType {
  public static final int RADIUS = 0;
  public static final int COLOR = 0xFF99CC;
  public static final String CHARACTER = "?";
  public static final String COMMAND = "support";

  public SupportMessage(String message) {
    super(message, RADIUS, COLOR, CHARACTER);
  }
}
