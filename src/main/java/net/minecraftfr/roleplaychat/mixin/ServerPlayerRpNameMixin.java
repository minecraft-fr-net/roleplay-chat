package net.minecraftfr.roleplaychat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;

/**
 * Surcharge {@code getPlayerListName()} côté serveur pour que la liste des joueurs
 * (touche Tab) affiche le pseudo RP à la place du vrai pseudo.
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
    RpNameStore.get(self.getServer())
        .getRpName(self.getUuid())
        .ifPresent(rpName -> cir.setReturnValue(Text.literal(rpName)));
  }
}
