package net.minecraftfr.roleplaychat.nameplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

/**
 * Stockage persistant des codes hexadécimaux anonymes, sauvegardé dans
 * {@code <world>/data/roleplay-chat-codes.dat} (NBT).
 *
 * <p>Chaque joueur reçoit un code unique {@code #XXXXXX} (hex pur 0-9 A-F)
 * à sa première connexion. Ce code remplace le username Minecraft comme
 * identifiant visible des autres joueurs.
 */
public class PlayerCodeStore extends PersistentState {

  private static final String DATA_KEY = "roleplay-chat-codes";
  private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
  private static final Random RANDOM = new Random();

  /** UUID → code (ex: {@code #15AACF}). */
  private final Map<UUID, String> byUuid = new HashMap<>();
  /** Code → UUID (index inverse pour unicité et lookup par commande). */
  private final Map<String, UUID> byCode = new HashMap<>();

  private static final Codec<PlayerCodeStore> CODEC =
      Codec.unboundedMap(
          Codec.STRING.xmap(UUID::fromString, UUID::toString),
          Codec.STRING
      ).xmap(
          map -> {
            PlayerCodeStore store = new PlayerCodeStore();
            map.forEach((uuid, code) -> {
              store.byUuid.put(uuid, code);
              store.byCode.put(code, uuid);
            });
            return store;
          },
          store -> store.byUuid
      );

  private static final PersistentStateType<PlayerCodeStore> TYPE =
      new PersistentStateType<>(DATA_KEY, PlayerCodeStore::new, CODEC, DataFixTypes.SAVED_DATA_SCOREBOARD);

  public static PlayerCodeStore get(MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
  }

  // -------------------------------------------------------------------------
  // API publique
  // -------------------------------------------------------------------------

  public Optional<String> getCode(UUID uuid) {
    return Optional.ofNullable(byUuid.get(uuid));
  }

  public @Nullable UUID getUuidByCode(String code) {
    return byCode.get(code);
  }

  public void setCode(UUID uuid, String code) {
    String old = byUuid.get(uuid);
    if (old != null) byCode.remove(old);

    byUuid.put(uuid, code);
    byCode.put(code, uuid);
    markDirty();
  }

  /**
   * Génère un code {@code #XXXXXX} aléatoire unique (hex pur 0-9 A-F).
   * Boucle jusqu'à trouver un code non encore attribué.
   */
  public String generateUniqueCode() {
    char[] chars = new char[7];
    chars[0] = '#';
    String code;
    do {
      for (int i = 1; i < 7; i++) {
        chars[i] = HEX_CHARS[RANDOM.nextInt(16)];
      }
      code = new String(chars);
    } while (byCode.containsKey(code));
    return code;
  }

  /** Retourne une vue non-modifiable de tous les codes (UUID → code). */
  public Map<UUID, String> getAll() {
    return Collections.unmodifiableMap(byUuid);
  }
}
