package net.minecraftfr.roleplaychat.nameplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Cache côté client des UUID dont le nom RP a été révélé au joueur local
 * via {@code /rp present} (reçu sous forme de {@link net.minecraftfr.roleplaychat.nameplate.RpNameRevealPayload}).
 *
 * <p>Vidé à la déconnexion pour éviter toute fuite entre serveurs.
 */
public final class RpNameRevealedCache {

    private static final Set<UUID> REVEALED = new HashSet<>();

    private RpNameRevealedCache() {}

    public static void add(UUID uuid) {
        REVEALED.add(uuid);
    }

    public static boolean contains(UUID uuid) {
        return REVEALED.contains(uuid);
    }

    public static void clear() {
        REVEALED.clear();
    }
}
