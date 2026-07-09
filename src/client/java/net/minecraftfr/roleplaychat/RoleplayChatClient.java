package net.minecraftfr.roleplaychat;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraftfr.roleplaychat.nameplate.OpenRpNameScreenPayload;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeClientCache;
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeUpdatePayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameClientCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealPayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealedCache;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;
import net.minecraftfr.roleplaychat.screen.RpNameInputScreen;

public class RoleplayChatClient implements ClientModInitializer {

	// Payload en attente d'ouverture de l'écran RP (null = pas d'écran à ouvrir)
	private static @Nullable OpenRpNameScreenPayload pendingRpNameScreen = null;

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
		// Planifier l'ouverture de l'écran RP : différé au premier tick en jeu pour ne pas
		// interrompre le DownloadingTerrainScreen pendant le chargement du monde
		ClientPlayNetworking.registerGlobalReceiver(OpenRpNameScreenPayload.ID,
				(payload, ctx) -> pendingRpNameScreen = payload);
		// Ouvrir l'écran dès que le joueur est en jeu (aucun écran de chargement ouvert).
		// Désactivé en mode gametest : les tests ouvrent l'écran directement via setScreen().
		if (System.getProperty("fabric.client.gametest") == null) {
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				if (pendingRpNameScreen != null && client.currentScreen == null && client.player != null) {
					client.setScreen(new RpNameInputScreen(pendingRpNameScreen.errorKey(), pendingRpNameScreen.errorArg()));
					pendingRpNameScreen = null;
				}
			});
		}
		// Réinitialiser tous les caches à la déconnexion (évite la pollution entre serveurs et tests)
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			pendingRpNameScreen = null;
			RpNameRevealedCache.clear();
			RpNameClientCache.clear();
			PlayerCodeClientCache.clear();
		});
	}
}
