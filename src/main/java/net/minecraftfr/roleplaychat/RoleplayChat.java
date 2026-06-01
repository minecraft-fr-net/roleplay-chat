package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraftfr.roleplaychat.command.RoleplayChatCommands;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleplayChat implements ModInitializer {
  public static final String MOD_ID = "roleplay-chat";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private final ChatManager chatManager = new ChatManager();

  @Override
  public void onInitialize() {
    RoleplayChatConfig.load();

    // Enregistrer le payload réseau S2C pour la synchronisation des pseudos RP
    PayloadTypeRegistry.playS2C().register(RpNameUpdatePayload.ID, RpNameUpdatePayload.CODEC);

    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, typeKey) -> {
      return chatManager.handleChatMessage(sender, message.getContent().getString(), message);
    });

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      RoleplayChatCommands.register(dispatcher);
    });

    // Envoyer tous les pseudos RP existants au joueur qui se connecte
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
      RpNameStore store = RpNameStore.get(server);
      store.getAll().forEach((uuid, rpName) ->
          ServerPlayNetworking.send(handler.player, new RpNameUpdatePayload(uuid, rpName)));
    });
  }
}
