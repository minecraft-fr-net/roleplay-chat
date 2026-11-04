package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftfr.roleplaychat.nameplate.RpNameTagHelper;

/**
 * Surcharge {@code getDisplayName()} côté client pour contrôler l'affichage
 * du nametag. Délègue la logique à {@link RpNameTagHelper}.
 */
@Mixin(Player.class)
public abstract class RpNameTagMixin {

  @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
  private void injectRpDisplayName(CallbackInfoReturnable<Component> cir) {
    if (!((Object) this instanceof AbstractClientPlayer self)) return;
    Minecraft mc = Minecraft.getInstance();
    boolean creative = mc.player != null && mc.player.getAbilities().instabuild;
    Component label = RpNameTagHelper.getDisplayName(self.getUUID(), creative);
    if (label != null) cir.setReturnValue(label);
  }
}
