package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftfr.roleplaychat.command.RoleplayChatCommands;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;
import net.minecraftfr.roleplaychat.nameplate.OpenRpNameScreenPayload;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeUpdatePayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameInputPayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealPayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameVisibilityStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleplayChat implements ModInitializer {
  public static final String MOD_ID = "roleplay-chat";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private final ChatManager chatManager = new ChatManager();

  @Override
  public void onInitialize() {
    RoleplayChatConfig.load();

    // Enregistrer les payloads réseau S2C
    PayloadTypeRegistry.playS2C().register(RpNameUpdatePayload.ID, RpNameUpdatePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(PlayerCodeUpdatePayload.ID, PlayerCodeUpdatePayload.CODEC);
    PayloadTypeRegistry.playS2C().register(RpNameRevealPayload.ID, RpNameRevealPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(OpenRpNameScreenPayload.ID, OpenRpNameScreenPayload.CODEC);
    // Enregistrer les payloads réseau C2S
    PayloadTypeRegistry.playC2S().register(RpNameInputPayload.ID, RpNameInputPayload.CODEC);

    // Traiter la saisie du pseudo RP depuis l'écran de connexion
    ServerPlayNetworking.registerGlobalReceiver(RpNameInputPayload.ID, (payload, ctx) -> {
      String name = payload.name().trim();
      ServerPlayer player = ctx.player();
      RpNameStore store = RpNameStore.get(ctx.server());

      if (name.isEmpty()) {
        ServerPlayNetworking.send(player, new OpenRpNameScreenPayload("screen.roleplay-chat.rp_name_input.error.empty", ""));
        return;
      }
      if (name.length() > 32) {
        ServerPlayNetworking.send(player, new OpenRpNameScreenPayload("screen.roleplay-chat.rp_name_input.error.too_long", ""));
        return;
      }
      if (store.isNameTaken(name, player.getUUID())) {
        ServerPlayNetworking.send(player, new OpenRpNameScreenPayload("screen.roleplay-chat.rp_name_input.error.taken", name));
        return;
      }

      store.setRpName(player.getUUID(), name);
      RpNameUpdatePayload rpPayload = new RpNameUpdatePayload(player.getUUID(), name);
      ctx.server().getPlayerList().getPlayers().forEach(p ->
          ServerPlayNetworking.send(p, rpPayload));
      ctx.server().getPlayerList().broadcastAll(
          new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
    });

    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, typeKey) -> {
      return chatManager.handleChatMessage(sender, message.decoratedContent().getString(), message);
    });

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      RoleplayChatCommands.register(dispatcher);
    });

    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
      // Synchroniser tous les pseudos RP existants vers le joueur qui se connecte
      // (inclus le mode créatif qui contourne la visibilité)
      RpNameStore rpStore = RpNameStore.get(server);
      rpStore.getAll().forEach((uuid, rpName) ->
          ServerPlayNetworking.send(handler.player, new RpNameUpdatePayload(uuid, rpName)));

      // Synchroniser les présentations déjà faites (quels UUID ce joueur peut voir)
      RpNameVisibilityStore visibilityStore = RpNameVisibilityStore.get(server);
      visibilityStore.getVisibleFor(handler.player.getUUID()).forEach(presenterUuid ->
          ServerPlayNetworking.send(handler.player, new RpNameRevealPayload(presenterUuid)));

      // Assigner un code hexadécimal unique à la première connexion
      PlayerCodeStore codeStore = PlayerCodeStore.get(server);
      if (codeStore.getCode(handler.player.getUUID()).isEmpty()) {
        String code = codeStore.generateUniqueCode();
        codeStore.setCode(handler.player.getUUID(), code);
        // Diffuser le nouveau code à tous les joueurs connectés (y compris le nouveau)
        PlayerCodeUpdatePayload newCodePayload = new PlayerCodeUpdatePayload(handler.player.getUUID(), code);
        server.getPlayerList().getPlayers().forEach(p ->
            ServerPlayNetworking.send(p, newCodePayload));
      }

      // Synchroniser tous les codes existants vers le joueur qui se connecte
      codeStore.getAll().forEach((uuid, code) ->
          ServerPlayNetworking.send(handler.player, new PlayerCodeUpdatePayload(uuid, code)));

      // Ouvrir l'écran de saisie si le joueur n'a pas encore de pseudo RP
      if (rpStore.getRpName(handler.player.getUUID()).isEmpty()) {
        ServerPlayNetworking.send(handler.player, new OpenRpNameScreenPayload("", ""));
      }
    });
  }
}
