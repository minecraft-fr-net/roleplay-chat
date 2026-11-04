package net.minecraftfr.roleplaychat.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;

/**
 * Permet aux commandes vanilla utilisant {@code GameProfileArgument} (ex: {@code /ban},
 * {@code /kick}, {@code /op}) d'accepter un code hexadécimal {@code #XXXXXX} comme
 * identifiant joueur en plus du username Minecraft.
 *
 * <p>Point d'injection : {@code method_9328} est la méthode privée statique synthétique
 * de {@code GameProfileArgument} qui résout un nom plain-text en
 * {@code Collection<NameAndId>} (nom intermediary, conservé tel quel avec les mappings Mojang).
 */
@Mixin(GameProfileArgument.class)
public abstract class GameProfileHexCodeMixin {

  private static final Pattern HEX_CODE_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

  @Inject(
      method = "method_9328(Ljava/lang/String;Lnet/minecraft/commands/CommandSourceStack;)Ljava/util/Collection;",
      at = @At("HEAD"),
      cancellable = true
  )
  private static void resolveHexCode(
      String name,
      CommandSourceStack source,
      CallbackInfoReturnable<Collection<NameAndId>> cir
  ) throws CommandSyntaxException {
    if (!HEX_CODE_PATTERN.matcher(name).matches()) return;

    MinecraftServer server = source.getServer();
    UUID targetUuid = PlayerCodeStore.get(server).getUuidByCode(name.toUpperCase());
    if (targetUuid == null) {
      throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
    }

    ServerPlayer online = server.getPlayerList().getPlayer(targetUuid);
    if (online != null) {
      cir.setReturnValue(Collections.singleton(online.nameAndId()));
      return;
    }

    java.util.Optional<NameAndId> cached = server.services().nameToIdCache().get(targetUuid);
    if (cached.isPresent()) {
      cir.setReturnValue(Collections.singleton(cached.get()));
      return;
    }

    throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
  }
}
