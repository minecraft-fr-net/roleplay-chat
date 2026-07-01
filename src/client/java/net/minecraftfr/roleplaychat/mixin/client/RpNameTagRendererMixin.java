package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.nameplate.RpNameTagHelper;

/**
 * Gestion avancée du nametag :
 * - Quand un pseudo RP est actif, affiche le code {@code #XXXXXX} en dessous (dans sa couleur hex)
 *   à la place du MC username. Quand seul le code est actif, rien n'est ajouté en dessous.
 * - Masque le nametag si un bloc bloque la ligne de vue.
 * - Si le joueur porte un item tagué {@code conceals_identity} : cache le nom RP (affiche {@code ?})
 *   mais conserve le code hex pour permettre les reports. Même si le joueur s'est déjà présenté,
 *   son identité RP reste masquée tant qu'il porte l'item.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class RpNameTagRendererMixin {

    private static final TagKey<Item> CONCEALS_IDENTITY =
        TagKey.of(RegistryKeys.ITEM, Identifier.of("roleplay-chat", "conceals_identity"));

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectRpPlayerName(AbstractClientPlayerEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        Text hex = RpNameTagHelper.getHexLabel(entity.getUuid());
        if (hex != null) state.playerName = hex;
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectLosOcclusion(AbstractClientPlayerEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null || mc.world == null || mc.player == null) return;
        if (entity == mc.player) return;
        if (mc.player.getAbilities().creativeMode) return;

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

    @Inject(
        method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectHeadgearOcclusion(AbstractClientPlayerEntity entity, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.getAbilities().creativeMode) return;
        ItemStack head = entity.getEquippedStack(EquipmentSlot.HEAD);
        if (head.isEmpty() || !head.isIn(CONCEALS_IDENTITY)) return;
        // Déjà caché par l'occlusion de vue (mur devant) — ne rien afficher du tout.
        if (state.nameLabelPos == null) return;
        // Cache le nom RP (même si déjà présenté) mais conserve le code hex pour les reports.
        state.displayName = Text.literal("?").styled(s -> s.withColor(Formatting.WHITE));
        // state.playerName conserve le code hex défini par injectRpPlayerName.
    }
}
