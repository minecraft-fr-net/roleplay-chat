package net.minecraftfr.roleplaychat.mixin;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;

/**
 * Fournit l'autocomplétion des codes {@code #XXXXXX} dans tous les arguments
 * de type entité. Dès que l'utilisateur tape {@code #}, les codes des joueurs
 * en ligne correspondant au préfixe sont proposés.
 */
@Mixin(EntityArgumentType.class)
public abstract class EntityArgumentTypeHexCodeMixin {

    @Inject(
        method = "listSuggestions(Lcom/mojang/brigadier/context/CommandContext;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("HEAD"),
        cancellable = true
    )
    private <S> void addHexCodeSuggestions(
            CommandContext<S> context,
            SuggestionsBuilder builder,
            CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
        String remaining = builder.getRemaining();
        if (remaining.isEmpty() || remaining.charAt(0) != '#') return;
        if (remaining.length() > 7) return;

        String partial = remaining.toUpperCase();
        for (int i = 1; i < partial.length(); i++) {
            char c = partial.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F'))) return;
        }

        if (!(context.getSource() instanceof ServerCommandSource source)) return;
        PlayerCodeStore.get(source.getServer()).getAll().forEach((uuid, code) -> {
            if (code.startsWith(partial)) builder.suggest(code);
        });
        cir.setReturnValue(builder.buildFuture());
    }
}
