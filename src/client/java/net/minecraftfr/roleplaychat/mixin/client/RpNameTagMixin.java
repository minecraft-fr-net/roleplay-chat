package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;

/**
 * Surcharge {@code getDisplayName()} côté client pour afficher le pseudo RP
 * dans le nametag au-dessus de la tête du joueur.
 */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class RpNameTagMixin {

  @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
  private void injectRpDisplayName(CallbackInfoReturnable<Text> cir) {
    AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
    String rp = RpNameClientCache.get(self.getUuid());
    if (rp != null) {
      cir.setReturnValue(Text.literal(rp));
    }
  }
}
