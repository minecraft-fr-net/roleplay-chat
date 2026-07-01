package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraftfr.roleplaychat.command.RoleplayChatCommands;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeUpdatePayload;
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

    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, typeKey) -> {
      return chatManager.handleChatMessage(sender, message.getContent().getString(), message);
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
      visibilityStore.getVisibleFor(handler.player.getUuid()).forEach(presenterUuid ->
          ServerPlayNetworking.send(handler.player, new RpNameRevealPayload(presenterUuid)));

      // Assigner un code hexadécimal unique à la première connexion
      PlayerCodeStore codeStore = PlayerCodeStore.get(server);
      if (codeStore.getCode(handler.player.getUuid()).isEmpty()) {
        String code = codeStore.generateUniqueCode();
        codeStore.setCode(handler.player.getUuid(), code);
        // Diffuser le nouveau code à tous les joueurs connectés (y compris le nouveau)
        PlayerCodeUpdatePayload newCodePayload = new PlayerCodeUpdatePayload(handler.player.getUuid(), code);
        server.getPlayerManager().getPlayerList().forEach(p ->
            ServerPlayNetworking.send(p, newCodePayload));
      }

      // Synchroniser tous les codes existants vers le joueur qui se connecte
      codeStore.getAll().forEach((uuid, code) ->
          ServerPlayNetworking.send(handler.player, new PlayerCodeUpdatePayload(uuid, code)));
    });
  }
}
