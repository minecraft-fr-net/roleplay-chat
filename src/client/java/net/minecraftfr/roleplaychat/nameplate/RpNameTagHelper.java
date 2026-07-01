package net.minecraftfr.roleplaychat.nameplate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Centralise la logique d'affichage du nametag RP pour éviter la duplication
 * entre {@link net.minecraftfr.roleplaychat.mixin.client.RpNameTagMixin} (label principal)
 * et {@link net.minecraftfr.roleplaychat.mixin.client.RpNameTagRendererMixin} (label secondaire).
 *
 * <p>Règles d'affichage :
 * <ul>
 *   <li>Mode créatif : affiche le nom RP s'il existe (bypass présentation)</li>
 *   <li>Nom RP révélé : affiche le nom RP</li>
 *   <li>Joueur connu (a un code) mais pas présenté : affiche {@code ?}</li>
 *   <li>Label secondaire : code {@code #XXXXXX} toujours visible (modération)</li>
 * </ul>
 */
public final class RpNameTagHelper {

    private RpNameTagHelper() {}

    /**
     * Retourne le label principal à afficher au-dessus de la tête du joueur,
     * ou {@code null} si aucun override n'est nécessaire (affichage vanilla).
     */
    public static @Nullable Text getDisplayName(java.util.UUID uuid, boolean creative) {
        String rp = RpNameClientCache.get(uuid);
        if (rp != null && (creative || RpNameRevealedCache.contains(uuid))) {
            return Text.literal(rp);
        }
        if (PlayerCodeClientCache.get(uuid) != null) {
            return Text.literal("?").styled(s -> s.withColor(Formatting.WHITE));
        }
        return null;
    }

    /**
     * Retourne le label secondaire (code hex) affiché en dessous du label principal,
     * ou {@code null} si le joueur n'a pas encore de code connu.
     */
    public static @Nullable Text getHexLabel(java.util.UUID uuid) {
        String code = PlayerCodeClientCache.get(uuid);
        if (code == null) return null;
        return Text.literal(code).styled(s -> s.withColor(Formatting.GRAY).withItalic(true));
    }
}
