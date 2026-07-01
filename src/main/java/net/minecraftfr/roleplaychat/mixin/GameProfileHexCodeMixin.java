package net.minecraftfr.roleplaychat.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;

/**
 * Permet aux commandes vanilla utilisant {@code GameProfileArgumentType} (ex: {@code /ban},
 * {@code /kick}, {@code /op}) d'accepter un code hexadécimal {@code #XXXXXX} comme
 * identifiant joueur en plus du username Minecraft.
 *
 * <p>Point d'injection : {@code method_9328} est la méthode privée statique synthétique
 * de {@code GameProfileArgumentType} qui résout un nom plain-text en
 * {@code Collection<GameProfile>} (nom Yarn 1.21.6+build.1, stable pour cette version).
 */
@Mixin(GameProfileArgumentType.class)
public abstract class GameProfileHexCodeMixin {

  private static final Pattern HEX_CODE_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

  @Inject(
      method = "method_9328(Ljava/lang/String;Lnet/minecraft/server/command/ServerCommandSource;)Ljava/util/Collection;",
      at = @At("HEAD"),
      cancellable = true
  )
  private static void resolveHexCode(
      String name,
      ServerCommandSource source,
      CallbackInfoReturnable<Collection<GameProfile>> cir
  ) throws CommandSyntaxException {
    if (!HEX_CODE_PATTERN.matcher(name).matches()) return;

    MinecraftServer server = source.getServer();
    UUID targetUuid = PlayerCodeStore.get(server).getUuidByCode(name.toUpperCase());
    if (targetUuid == null) {
      throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
    }

    ServerPlayerEntity online = server.getPlayerManager().getPlayer(targetUuid);
    if (online != null) {
      cir.setReturnValue(Collections.singleton(online.getGameProfile()));
      return;
    }

    java.util.Optional<GameProfile> cached = server.getUserCache().getByUuid(targetUuid);
    if (cached.isPresent()) {
      cir.setReturnValue(Collections.singleton(cached.get()));
      return;
    }

    throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
  }
}
