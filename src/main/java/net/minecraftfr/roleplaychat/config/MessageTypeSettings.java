package net.minecraftfr.roleplaychat.config;

import java.util.Collections;
import java.util.List;

/**
 * Paramètres effectifs pour un type de message (après validation du JSON).
 */
public final class MessageTypeSettings {
  private final int radius;
  private final int colorRgb;
  private final List<String> characters;

  public MessageTypeSettings(int radius, int colorRgb, List<String> characters) {
    this.radius = radius;
    this.colorRgb = colorRgb;
    this.characters = List.copyOf(characters);
  }

  public int radius() {
    return radius;
  }

  public int colorRgb() {
    return colorRgb;
  }

  public List<String> characters() {
    return characters;
  }

  public String firstPrefix() {
    return characters.isEmpty() ? "" : characters.get(0);
  }

  public boolean messageMatchesPrefix(String message) {
    for (String prefix : characters) {
      if (message.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  public int prefixLengthFor(String message) {
    for (String prefix : characters) {
      if (message.startsWith(prefix)) {
        return prefix.length();
      }
    }
    return 0;
  }

  public static MessageTypeSettings speakDefault() {
    return new MessageTypeSettings(30, 0xFFFFFF, Collections.emptyList());
  }

  public static MessageTypeSettings whisperDefault() {
    return new MessageTypeSettings(4, 0xCC33CC, List.of("«", "\""));
  }

  public static MessageTypeSettings shoutDefault() {
    return new MessageTypeSettings(80, 0xCC3300, List.of("!"));
  }

  public static MessageTypeSettings actionDefault() {
    return new MessageTypeSettings(25, 0x33CC33, List.of("*"));
  }

  public static MessageTypeSettings oocDefault() {
    return new MessageTypeSettings(60, 0xAEC1D5, List.of("("));
  }

  public static MessageTypeSettings globalOocDefault() {
    return new MessageTypeSettings(0, 0xAEC1D5, List.of("["));
  }

  public static MessageTypeSettings supportDefault() {
    return new MessageTypeSettings(0, 0xFF99CC, List.of("?"));
  }
}
