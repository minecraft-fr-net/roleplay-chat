package net.minecraftfr.roleplaychat.chatTypeMessage;

public class WhisperMessage extends MessageType {
  public static final int RADIUS = 4;
  public static final int COLOR = 0xCC33CC;
  public static final String[] CHARACTERS = {"«", "\""};

  public WhisperMessage() {
    super(RADIUS, COLOR, null);
  }

  @Override
  public boolean canBeSend(String message) {
    for (String character : CHARACTERS) {
      if (message.startsWith(character)) {
        return true;
      }
    }
    return false;
  }
}
