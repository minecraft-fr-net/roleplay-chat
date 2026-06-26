package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;

/**
 * Quand un pseudo RP est actif, injecte le MC username dans {@code state.playerName}
 * pour qu'il apparaisse en dessous du pseudo RP dans le nametag.
 *
 * <p>Le {@code PlayerEntityRenderer} rend {@code state.playerName} en bas,
 * puis translate vers le haut et rend le label principal ({@code getDisplayName()}) au-dessus.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class RpNameTagRendererMixin {

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectRpPlayerName(AbstractClientPlayerEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        String rp = RpNameClientCache.get(entity.getUuid());
        if (rp != null) {
            state.playerName = entity.getName().copy().formatted(Formatting.GRAY, Formatting.ITALIC);
        }
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectLosOcclusion(AbstractClientPlayerEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null || mc.world == null || mc.player == null) return;
        if (entity == mc.player) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d eyePos = entity.getEyePos();

        BlockHitResult hit = entity.clientWorld.raycast(new RaycastContext(
            cameraPos,
            eyePos,
            RaycastContext.ShapeType.VISUAL,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            state.displayName = null;
            state.nameLabelPos = null;
            state.playerName = null;
        }
    }
}
