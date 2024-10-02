package net.minecraftfr.roleplaychat.chatTypeMessage;

public class WhisperMessage extends MessageType {
  public static final int RADIUS = 4;
  public static final int COLOR = 0xCC33CC;
  public static final String[] CHARACTERS = {"«", "\""};
  public static final String COMMAND = "whisper";

  public WhisperMessage(String message) {
    super(message, RADIUS, COLOR, null);
  }

  @Override
  public boolean canBeSend() {
    for (String character : CHARACTERS) {
      if (message.startsWith(character)) {
        return true;
      }
    }
    return false;
  }
}
