package net.minecraftfr.roleplaychat.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftfr.roleplaychat.RoleplayChat;

public final class RoleplayChatConfig {
  private static final String FILE_NAME = "roleplay-chat.json";
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static volatile RoleplayChatConfig instance = defaults();

  private final MessageTypeSettings speak;
  private final MessageTypeSettings whisper;
  private final MessageTypeSettings shout;
  private final MessageTypeSettings action;
  private final MessageTypeSettings ooc;
  private final MessageTypeSettings globalOoc;
  private final MessageTypeSettings support;
  private final MessageTypeSettings roll;

  private RoleplayChatConfig(
      MessageTypeSettings speak,
      MessageTypeSettings whisper,
      MessageTypeSettings shout,
      MessageTypeSettings action,
      MessageTypeSettings ooc,
      MessageTypeSettings globalOoc,
      MessageTypeSettings support,
      MessageTypeSettings roll) {
    this.speak = speak;
    this.whisper = whisper;
    this.shout = shout;
    this.action = action;
    this.ooc = ooc;
    this.globalOoc = globalOoc;
    this.support = support;
    this.roll = roll;
  }

  public static RoleplayChatConfig get() {
    return instance;
  }

  public static RoleplayChatConfig defaults() {
    return new RoleplayChatConfig(
        MessageTypeSettings.speakDefault(),
        MessageTypeSettings.whisperDefault(),
        MessageTypeSettings.shoutDefault(),
        MessageTypeSettings.actionDefault(),
        MessageTypeSettings.oocDefault(),
        MessageTypeSettings.globalOocDefault(),
        MessageTypeSettings.supportDefault(),
        MessageTypeSettings.rollDefault());
  }

  public static void load() {
    Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    if (!Files.isRegularFile(path)) {
      instance = defaults();
      save(instance, path);
      RoleplayChat.LOGGER.info("[roleplay-chat] Created default config at {}", path);
      return;
    }
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      instance = parseAndValidate(root);
      RoleplayChat.LOGGER.info("[roleplay-chat] Loaded config from {}", path);
    } catch (Exception e) {
      RoleplayChat.LOGGER.error("[roleplay-chat] Failed to load config, using defaults: {}", e.toString());
      instance = defaults();
    }
  }

  public static boolean reload() {
    Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    if (!Files.isRegularFile(path)) {
      instance = defaults();
      save(instance, path);
      return true;
    }
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      JsonObject root = GSON.fromJson(reader, JsonObject.class);
      instance = parseAndValidate(root);
      return true;
    } catch (Exception e) {
      RoleplayChat.LOGGER.error("[roleplay-chat] Reload failed, keeping previous config: {}", e.toString());
      return false;
    }
  }

  private static RoleplayChatConfig parseAndValidate(JsonObject root) {
    if (root == null) {
      RoleplayChat.LOGGER.warn("[roleplay-chat] Config root is null, using defaults");
      return defaults();
    }
    MessageTypeSettings speak = parseEntry("speak", root, MessageTypeSettings.speakDefault(), true);
    MessageTypeSettings whisper = parseEntry("whisper", root, MessageTypeSettings.whisperDefault(), false);
    MessageTypeSettings shout = parseEntry("shout", root, MessageTypeSettings.shoutDefault(), false);
    MessageTypeSettings action = parseEntry("action", root, MessageTypeSettings.actionDefault(), false);
    MessageTypeSettings ooc = parseEntry("ooc", root, MessageTypeSettings.oocDefault(), false);
    MessageTypeSettings globalOoc = parseEntry("globalOoc", root, MessageTypeSettings.globalOocDefault(), false);
    MessageTypeSettings support = parseEntry("support", root, MessageTypeSettings.supportDefault(), false);
    MessageTypeSettings roll = parseEntry("roll", root, MessageTypeSettings.rollDefault(), true);
    return new RoleplayChatConfig(speak, whisper, shout, action, ooc, globalOoc, support, roll);
  }

  private static MessageTypeSettings parseEntry(
      String key,
      JsonObject root,
      MessageTypeSettings fallback,
      boolean allowEmptyPrefixes) {
    if (!root.has(key) || !root.get(key).isJsonObject()) {
      RoleplayChat.LOGGER.warn("[roleplay-chat] Missing or invalid \"{}\", using defaults", key);
      return fallback;
    }
    JsonObject o = root.getAsJsonObject(key);
    int radius = readRadius(key, o, fallback.radius());
    int color = readColor(key, o, fallback.colorRgb());
    List<String> characters = readCharacters(key, o, fallback.characters());
    if (!allowEmptyPrefixes && characters.isEmpty()) {
      RoleplayChat.LOGGER.warn("[roleplay-chat] \"{}\".characters must not be empty, using defaults", key);
      return fallback;
    }
    return new MessageTypeSettings(radius, color, characters);
  }

  private static int readRadius(String key, JsonObject o, int fallback) {
    if (!o.has("radius") || !o.get("radius").isJsonPrimitive()) {
      return fallback;
    }
    try {
      int r = o.get("radius").getAsInt();
      if (r < 0) {
        RoleplayChat.LOGGER.warn("[roleplay-chat] \"{}\".radius must be >= 0, using default", key);
        return fallback;
      }
      return r;
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static int readColor(String key, JsonObject o, int fallback) {
    if (!o.has("color")) {
      return fallback;
    }
    JsonElement el = o.get("color");
    try {
      if (el.isJsonPrimitive()) {
        if (el.getAsJsonPrimitive().isNumber()) {
          return el.getAsInt() & 0xFFFFFF;
        }
        if (el.getAsJsonPrimitive().isString()) {
          return parseHexColor(el.getAsString(), fallback);
        }
      }
    } catch (Exception e) {
      RoleplayChat.LOGGER.warn("[roleplay-chat] \"{}\".color invalid, using default", key);
    }
    return fallback;
  }

  private static int parseHexColor(String raw, int fallback) {
    if (raw == null || raw.isEmpty()) {
      return fallback;
    }
    String s = raw.startsWith("#") ? raw.substring(1) : raw;
    if (s.length() != 6) {
      RoleplayChat.LOGGER.warn("[roleplay-chat] color hex must be 6 digits, got: {}", raw);
      return fallback;
    }
    try {
      return Integer.parseUnsignedInt(s, 16) & 0xFFFFFF;
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static List<String> readCharacters(String key, JsonObject o, List<String> fallback) {
    if (!o.has("characters") || !o.get("characters").isJsonArray()) {
      return new ArrayList<>(fallback);
    }
    List<String> out = new ArrayList<>();
    for (JsonElement el : o.getAsJsonArray("characters")) {
      if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
        out.add(el.getAsString());
      }
    }
    return out;
  }

  private static void save(RoleplayChatConfig config, Path path) {
    try {
      Files.createDirectories(path.getParent());
      JsonObject root = new JsonObject();
      root.add("speak", toJsonObject(config.speak));
      root.add("whisper", toJsonObject(config.whisper));
      root.add("shout", toJsonObject(config.shout));
      root.add("action", toJsonObject(config.action));
      root.add("ooc", toJsonObject(config.ooc));
      root.add("globalOoc", toJsonObject(config.globalOoc));
      root.add("support", toJsonObject(config.support));
      root.add("roll", toJsonObject(config.roll));
      try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        GSON.toJson(root, w);
      }
    } catch (IOException e) {
      RoleplayChat.LOGGER.error("[roleplay-chat] Failed to write config: {}", e.toString());
    }
  }

  private static JsonObject toJsonObject(MessageTypeSettings s) {
    JsonObject o = new JsonObject();
    o.addProperty("radius", s.radius());
    o.addProperty("color", s.colorRgb());
    var arr = new com.google.gson.JsonArray();
    for (String c : s.characters()) {
      arr.add(c);
    }
    o.add("characters", arr);
    return o;
  }

  public boolean wouldBeSpeakExcludingShoutWhisperAction(String message) {
    return !shout.messageMatchesPrefix(message)
        && !whisper.messageMatchesPrefix(message)
        && !action.messageMatchesPrefix(message);
  }

  public MessageTypeSettings speak() {
    return speak;
  }

  public MessageTypeSettings whisper() {
    return whisper;
  }

  public MessageTypeSettings shout() {
    return shout;
  }

  public MessageTypeSettings action() {
    return action;
  }

  public MessageTypeSettings ooc() {
    return ooc;
  }

  public MessageTypeSettings globalOoc() {
    return globalOoc;
  }

  public MessageTypeSettings support() {
    return support;
  }

  public MessageTypeSettings roll() {
    return roll;
  }
}
