package net.minecraftfr.roleplaychat.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.command.EntitySelectorReader;

/**
 * Permet d'utiliser un code {@code #XXXXXX} dans les sélecteurs d'entités
 * ({@code /damage}, {@code /give}, {@code /kill}, {@code /tp}, etc.).
 *
 * <p>{@code readRegular()} appelle {@code StringReader.readString()} qui rejette
 * {@code #} immédiatement (caractère interdit en nom non-quoté). On intercepte
 * avant cette lecture pour consommer les 7 caractères et configurer le sélecteur
 * comme si c'était un {@code playerName} ordinaire — la résolution réelle est
 * faite ensuite par {@link PlayerManagerHexCodeMixin}.
 */
@Mixin(EntitySelectorReader.class)
public abstract class EntitySelectorHexCodeMixin {

    @Shadow private StringReader reader;
    @Shadow private String playerName;
    @Shadow private boolean includesNonPlayers;
    @Shadow private int limit;

    @Inject(method = "readRegular()V", at = @At("HEAD"), cancellable = true)
    private void handleHexCodeInSelector(CallbackInfo ci) throws CommandSyntaxException {
        if (!reader.canRead(7)) return;
        int cursor = reader.getCursor();
        String input = reader.getString();
        if (input.charAt(cursor) != '#') return;
        for (int i = 1; i <= 6; i++) {
            char c = input.charAt(cursor + i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))) return;
        }
        // Ne pas accepter si suivi d'un caractère valide en nom (ex: #094921g)
        if (input.length() > cursor + 7 && StringReader.isAllowedInUnquotedString(input.charAt(cursor + 7))) return;
        reader.setCursor(cursor + 7);
        this.playerName = input.substring(cursor, cursor + 7).toUpperCase();
        this.includesNonPlayers = false;
        this.limit = 1;
        ci.cancel();
    }
}
