package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraftfr.roleplaychat.command.RoleplayChatCommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleplayChat implements ModInitializer {
  public static final String MOD_ID = "roleplay-chat";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private final ChatManager chatManager = new ChatManager();

  @Override
  public void onInitialize() {
    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, typeKey) -> {
      return chatManager.handleChatMessage(sender, message.getContent().getString());
    });

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      RoleplayChatCommands.register(dispatcher);
    });
  }
}
