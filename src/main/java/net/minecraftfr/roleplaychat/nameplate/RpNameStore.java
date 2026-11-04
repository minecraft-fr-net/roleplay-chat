package net.minecraftfr.roleplaychat.nameplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Stockage persistant des pseudos RP, sauvegardé dans
 * {@code <world>/data/roleplay-chat-rpnames.dat} (NBT).
 *
 * <p>Chargé/sauvegardé automatiquement par Minecraft via {@link net.minecraft.world.level.storage.DimensionDataStorage}.
 */
public class RpNameStore extends SavedData {

  private static final String DATA_KEY = "roleplay-chat-rpnames";

  /** UUID → pseudo RP (tel qu'entré par le joueur). */
  private final Map<UUID, String> byUuid   = new HashMap<>();
  /** Pseudo RP en minuscules → UUID (pour la vérification d'unicité). */
  private final Map<String, UUID> byRpName = new HashMap<>();

  /** Codec de (dé)sérialisation : Map&lt;UUID, rpName&gt; ↔ compound NBT avec clés UUID. */
  private static final Codec<RpNameStore> CODEC =
      Codec.unboundedMap(
          Codec.STRING.xmap(UUID::fromString, UUID::toString),
          Codec.STRING
      ).xmap(
          map -> {
            RpNameStore store = new RpNameStore();
            map.forEach((uuid, rpName) -> {
              store.byUuid.put(uuid, rpName);
              store.byRpName.put(rpName.toLowerCase(), uuid);
            });
            return store;
          },
          store -> store.byUuid
      );

  private static final SavedDataType<RpNameStore> TYPE =
      new SavedDataType<>(DATA_KEY, RpNameStore::new, CODEC, DataFixTypes.SAVED_DATA_SCOREBOARD);

  public static RpNameStore get(MinecraftServer server) {
    return server.overworld().getDataStorage().computeIfAbsent(TYPE);
  }

  // -------------------------------------------------------------------------
  // API publique
  // -------------------------------------------------------------------------

  public Optional<String> getRpName(UUID uuid) {
    return Optional.ofNullable(byUuid.get(uuid));
  }

  /**
   * Vérifie si le pseudo RP est déjà pris (comparaison insensible à la casse),
   * en ignorant le joueur {@code ownerUuid} (pour qu'un joueur puisse
   * redéfinir son propre nom sans conflit).
   */
  public boolean isNameTaken(String rpName, UUID ownerUuid) {
    UUID existing = byRpName.get(rpName.toLowerCase());
    return existing != null && !existing.equals(ownerUuid);
  }

  public void setRpName(UUID uuid, String rpName) {
    // Retirer l'ancien nom de l'index inversé
    String old = byUuid.get(uuid);
    if (old != null) byRpName.remove(old.toLowerCase());

    byUuid.put(uuid, rpName);
    byRpName.put(rpName.toLowerCase(), uuid);
    setDirty();
  }

  /** Retourne une vue non-modifiable de tous les pseudos RP (UUID → rpName). */
  public Map<UUID, String> getAll() {
    return Collections.unmodifiableMap(byUuid);
  }
}
