package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.network.ServerPlayerEntity;

public class RoleplayChat implements ModInitializer {
	public static final String MOD_ID = "roleplay-chat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final ChatManager chatManager = new ChatManager();

	@Override
	public void onInitialize() {
		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
      handleChatMessage(sender, message.toString());
    });
	}

	private void handleChatMessage(ServerPlayerEntity player, String message) {
		// Check if the message has no trigger character (normal speak)
		if (!message.startsWith("!") && !message.startsWith("«") && !message.startsWith("*")) {
			chatManager.handleSpeakMessage(player, message);
		}
	}
}
