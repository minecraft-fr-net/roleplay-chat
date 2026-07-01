package net.minecraftfr.roleplaychat.nameplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

/**
 * Stockage persistant des relations de présentation RP.
 * Sauvegardé dans {@code <world>/data/roleplay-chat-visibility.dat}.
 *
 * <p>Structure : observateur → ensemble des UUID qui se sont présentés à lui.
 * Quand Elrich se présente à Basile via {@code /rp present}, l'UUID d'Elrich
 * est ajouté au set de Basile — Basile verra alors le nom RP d'Elrich.
 */
public class RpNameVisibilityStore extends PersistentState {

    private static final String DATA_KEY = "roleplay-chat-visibility";

    private static final Codec<UUID> UUID_CODEC =
        Codec.STRING.xmap(UUID::fromString, UUID::toString);

    /** observateur UUID → ensemble des UUID présentés à lui */
    private final Map<UUID, Set<UUID>> visibility = new HashMap<>();

    private static final Codec<RpNameVisibilityStore> CODEC =
        Codec.unboundedMap(UUID_CODEC, UUID_CODEC.listOf()).xmap(
            map -> {
                RpNameVisibilityStore store = new RpNameVisibilityStore();
                map.forEach((observer, presenters) ->
                    store.visibility.put(observer, new HashSet<>(presenters)));
                return store;
            },
            store -> {
                Map<UUID, List<UUID>> result = new HashMap<>();
                store.visibility.forEach((k, v) -> result.put(k, new ArrayList<>(v)));
                return result;
            }
        );

    private static final PersistentStateType<RpNameVisibilityStore> TYPE =
        new PersistentStateType<>(DATA_KEY, RpNameVisibilityStore::new, CODEC, DataFixTypes.SAVED_DATA_SCOREBOARD);

    public static RpNameVisibilityStore get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    // -------------------------------------------------------------------------
    // API publique
    // -------------------------------------------------------------------------

    /**
     * Enregistre que {@code presenter} s'est présenté à {@code observer}.
     * L'observateur pourra désormais voir le nom RP du présentateur.
     */
    public void addVisibility(UUID observer, UUID presenter) {
        visibility.computeIfAbsent(observer, k -> new HashSet<>()).add(presenter);
        markDirty();
    }

    public boolean canSee(UUID observer, UUID presenter) {
        Set<UUID> set = visibility.get(observer);
        return set != null && set.contains(presenter);
    }

    /** Retourne une vue non-modifiable des UUID dont le nom RP est visible par {@code observer}. */
    public Set<UUID> getVisibleFor(UUID observer) {
        return Collections.unmodifiableSet(
            visibility.getOrDefault(observer, Collections.emptySet()));
    }
}
