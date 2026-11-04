package net.minecraftfr.roleplaychat.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
@Mixin(AvatarRenderer.class)
public abstract class RpNameTagRendererMixin {

    private static final TagKey<Item> CONCEALS_IDENTITY =
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("roleplay-chat", "conceals_identity"));

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectRpPlayerName(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        Component hex = RpNameTagHelper.getHexLabel(entity.getUUID());
        if (hex != null) state.scoreText = hex;
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectLosOcclusion(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.level == null || mc.player == null) return;
        if (entity == mc.player) return;
        if (mc.player.getAbilities().instabuild) return;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        Vec3 eyePos = entity.getEyePosition();

        BlockHitResult hit = entity.level().clip(new ClipContext(
            cameraPos,
            eyePos,
            ClipContext.Block.VISUAL,
            ClipContext.Fluid.NONE,
            mc.player
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            state.nameTag = null;
            state.nameTagAttachment = null;
            state.scoreText = null;
        }
    }

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("RETURN")
    )
    private void injectHeadgearOcclusion(Avatar entity, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getAbilities().instabuild) return;
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty() || !head.is(CONCEALS_IDENTITY)) return;
        // Déjà caché par l'occlusion de vue (mur devant) — ne rien afficher du tout.
        if (state.nameTagAttachment == null) return;
        // Cache le nom RP (même si déjà présenté) mais conserve le code hex pour les reports.
        state.nameTag = Component.literal("?").withStyle(s -> s.withColor(ChatFormatting.WHITE));
        // state.playerName conserve le code hex défini par injectRpPlayerName.
    }
}
