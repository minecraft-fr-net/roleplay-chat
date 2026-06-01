package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;

/**
 * Surcharge {@code getDisplayName()} côté client pour afficher le pseudo RP
 * dans le nametag au-dessus de la tête du joueur.
 *
 * <p>Format affiché :
 * <pre>
 *   Elara          ← pseudo RP (blanc, taille normale)
 *   DevPlayer      ← username Minecraft (gris italique, pour la modération)
 * </pre>
 *
 * <p>La méthode est déclarée dans {@link PlayerEntity} (pas dans
 * {@code AbstractClientPlayerEntity}), donc on cible {@code PlayerEntity}
 * avec un guard pour ne s'appliquer qu'aux joueurs clients.
 */
@Mixin(PlayerEntity.class)
public abstract class RpNameTagMixin {

  @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
  private void injectRpDisplayName(CallbackInfoReturnable<Text> cir) {
    if (!((Object) this instanceof AbstractClientPlayerEntity self)) return;
    String rp = RpNameClientCache.get(self.getUuid());
    if (rp != null) {
      String mcName = self.getName().getString();
      MutableText label = Text.literal(rp)
          .append(Text.literal("\n" + mcName)
              .formatted(Formatting.GRAY, Formatting.ITALIC));
      cir.setReturnValue(label);
    }
  }
}
