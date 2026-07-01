package net.minecraftfr.roleplaychat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;

/**
 * Surcharge {@code getPlayerListName()} côté serveur pour que la liste des joueurs
 * (touche Tab) affiche le pseudo RP ou le code hexadécimal à la place du vrai pseudo.
 *
 * <p>Minecraft envoie automatiquement la valeur de cette méthode dans le paquet
 * {@code PlayerListS2CPacket} lors de la connexion et lors des mises à jour.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerRpNameMixin {

  @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
  private void injectRpListName(CallbackInfoReturnable<Text> cir) {
    ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
    if (self.getServer() == null) return;

    String rpName = RpNameStore.get(self.getServer()).getRpName(self.getUuid()).orElse(null);
    if (rpName != null) {
      cir.setReturnValue(Text.literal(rpName));
      return;
    }

    PlayerCodeStore.get(self.getServer()).getCode(self.getUuid())
        .ifPresent(code -> cir.setReturnValue(Text.literal(code)));
  }
}
