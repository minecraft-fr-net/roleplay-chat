package net.minecraftfr.roleplaychat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeClientCache;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeUpdatePayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealPayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealedCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;

public class RoleplayChatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Réception des pseudos RP envoyés par le serveur
		ClientPlayNetworking.registerGlobalReceiver(RpNameUpdatePayload.ID,
				(payload, ctx) -> RpNameClientCache.set(payload.uuid(), payload.rpName()));
		// Réception des codes hexadécimaux envoyés par le serveur
		ClientPlayNetworking.registerGlobalReceiver(PlayerCodeUpdatePayload.ID,
				(payload, ctx) -> PlayerCodeClientCache.set(payload.uuid(), payload.code()));
		// Réception des autorisations de visibilité (présentations)
		ClientPlayNetworking.registerGlobalReceiver(RpNameRevealPayload.ID,
				(payload, ctx) -> RpNameRevealedCache.add(payload.presenterUuid()));
		// Réinitialiser tous les caches à la déconnexion (évite la pollution entre serveurs et tests)
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			RpNameRevealedCache.clear();
			RpNameClientCache.clear();
			PlayerCodeClientCache.clear();
		});
	}
}