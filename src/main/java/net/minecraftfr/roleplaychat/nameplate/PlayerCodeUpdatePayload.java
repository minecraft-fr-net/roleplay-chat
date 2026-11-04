package net.minecraftfr.roleplaychat.nameplate;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet serveur → client diffusé quand un joueur reçoit ou change son code hexadécimal.
 * Envoyé aussi en masse au moment de la connexion pour synchroniser tous les codes existants.
 */
public record PlayerCodeUpdatePayload(UUID uuid, String code) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<PlayerCodeUpdatePayload> ID =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("roleplay-chat", "player_code_update"));

  public static final StreamCodec<FriendlyByteBuf, PlayerCodeUpdatePayload> CODEC =
      StreamCodec.ofMember(
          (value, buf) -> {
            buf.writeUUID(value.uuid());
            buf.writeUtf(value.code());
          },
          buf -> new PlayerCodeUpdatePayload(buf.readUUID(), buf.readUtf())
      );

  @Override
  public CustomPacketPayload.Type<PlayerCodeUpdatePayload> type() {
    return ID;
  }
}
