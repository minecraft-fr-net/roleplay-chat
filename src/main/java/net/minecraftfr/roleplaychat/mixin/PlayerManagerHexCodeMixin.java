package net.minecraftfr.roleplaychat.mixin;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;

/**
 * Permet d'utiliser un code {@code #XXXXXX} partout où Minecraft attend un
 * nom de joueur via {@link PlayerManager#getPlayer(String)} — notamment les
 * sélecteurs d'entités ({@code /damage}, {@code /give}, {@code /kill},
 * {@code /tp}, etc.).
 */
@Mixin(PlayerManager.class)
public abstract class PlayerManagerHexCodeMixin {

    @Shadow private MinecraftServer server;

    @Shadow public abstract @Nullable ServerPlayerEntity getPlayer(UUID uuid);

    @Inject(
        method = "getPlayer(Ljava/lang/String;)Lnet/minecraft/server/network/ServerPlayerEntity;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void resolveHexCodePlayer(String name, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (name == null || name.length() != 7 || name.charAt(0) != '#') return;
        UUID uuid = PlayerCodeStore.get(server).getUuidByCode(name.toUpperCase());
        if (uuid == null) return;
        ServerPlayerEntity player = getPlayer(uuid);
        if (player != null) cir.setReturnValue(player);
    }
}
