package net.minecraftfr.roleplaychat.mixin;

import com.mojang.brigadier.StringReader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Autorise {@code #} comme caractère valide dans les chaînes non-quotées de
 * Brigadier, ce qui permet d'utiliser {@code #094921} comme identifiant joueur
 * dans tous les arguments de type entité ({@code /damage #094921 ...}).
 *
 * <p>Sans ce patch, {@code StringReader.readUnquotedString()} s'arrête
 * immédiatement sur {@code #}, produisant une chaîne vide et une erreur de
 * syntaxe côté client avant même que le serveur soit contacté.
 */
@Mixin(value = StringReader.class, remap = false)
public class StringReaderHexCodeMixin {

    @Inject(
        method = "isAllowedInUnquotedString(C)Z",
        at = @At("RETURN"),
        cancellable = true,
        remap = false
    )
    private static void allowHash(char c, CallbackInfoReturnable<Boolean> cir) {
        if (c == '#') cir.setReturnValue(true);
    }
}
