package net.minecraftfr.roleplaychat.nameplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

/**
 * Cache côté client des pseudos RP reçus via {@link net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload}.
 * Alimenté dans {@link net.minecraftfr.roleplaychat.RoleplayChatClient}.
 */
public final class RpNameClientCache {

  private static final Map<UUID, String> NAMES = new HashMap<>();

  private RpNameClientCache() {}

  public static void set(UUID uuid, String rpName) {
    NAMES.put(uuid, rpName);
  }

  public static @Nullable String get(UUID uuid) {
    return NAMES.get(uuid);
  }

  public static void clear() {
    NAMES.clear();
  }
}
