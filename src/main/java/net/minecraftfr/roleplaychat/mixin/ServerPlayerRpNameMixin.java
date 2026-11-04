package net.minecraftfr.roleplaychat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;

/**
 * Surcharge {@code getPlayerListName()} côté serveur pour que la liste des joueurs
 * (touche Tab) affiche le pseudo RP ou le code hexadécimal à la place du vrai pseudo.
 *
 * <p>Minecraft envoie automatiquement la valeur de cette méthode dans le paquet
 * {@code PlayerListS2CPacket} lors de la connexion et lors des mises à jour.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRpNameMixin {

  @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
  private void injectRpListName(CallbackInfoReturnable<Component> cir) {
    ServerPlayer self = (ServerPlayer) (Object) this;
    if (self.level().getServer() == null) return;

    String rpName = RpNameStore.get(self.level().getServer()).getRpName(self.getUUID()).orElse(null);
    if (rpName != null) {
      cir.setReturnValue(Component.literal(rpName));
      return;
    }

    PlayerCodeStore.get(self.level().getServer()).getCode(self.getUUID())
        .ifPresent(code -> cir.setReturnValue(Component.literal(code)));
  }
}
