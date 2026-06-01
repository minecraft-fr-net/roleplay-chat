package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;

public class RoleplayChatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Réception des pseudos RP envoyés par le serveur
		ClientPlayNetworking.registerGlobalReceiver(RpNameUpdatePayload.ID,
				(payload, ctx) -> RpNameClientCache.set(payload.uuid(), payload.rpName()));
	}
}