package net.minecraftfr.roleplaychat.nameplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

/**
 * Cache côté client des codes hexadécimaux reçus via {@link PlayerCodeUpdatePayload}.
 * Alimenté dans {@link net.minecraftfr.roleplaychat.RoleplayChatClient}.
 */
public final class PlayerCodeClientCache {

  private static final Map<UUID, String> CODES = new HashMap<>();

  private PlayerCodeClientCache() {}

  public static void set(UUID uuid, String code) {
    CODES.put(uuid, code);
  }

  public static @Nullable String get(UUID uuid) {
    return CODES.get(uuid);
  }

  public static void clear() {
    CODES.clear();
  }
}
