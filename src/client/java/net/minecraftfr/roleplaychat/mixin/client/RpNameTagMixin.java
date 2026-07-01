package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.nameplate.RpNameTagHelper;

/**
 * Surcharge {@code getDisplayName()} côté client pour contrôler l'affichage
 * du nametag. Délègue la logique à {@link RpNameTagHelper}.
 */
@Mixin(PlayerEntity.class)
public abstract class RpNameTagMixin {

  @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
  private void injectRpDisplayName(CallbackInfoReturnable<Text> cir) {
    if (!((Object) this instanceof AbstractClientPlayerEntity self)) return;
    MinecraftClient mc = MinecraftClient.getInstance();
    boolean creative = mc.player != null && mc.player.getAbilities().creativeMode;
    Text label = RpNameTagHelper.getDisplayName(self.getUuid(), creative);
    if (label != null) cir.setReturnValue(label);
  }
}
