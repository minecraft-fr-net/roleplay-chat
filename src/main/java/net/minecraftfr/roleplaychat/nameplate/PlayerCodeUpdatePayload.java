package net.minecraftfr.roleplaychat.nameplate;

import java.util.UUID;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Paquet serveur → client diffusé quand un joueur reçoit ou change son code hexadécimal.
 * Envoyé aussi en masse au moment de la connexion pour synchroniser tous les codes existants.
 */
public record PlayerCodeUpdatePayload(UUID uuid, String code) implements CustomPayload {

  public static final CustomPayload.Id<PlayerCodeUpdatePayload> ID =
      new CustomPayload.Id<>(Identifier.of("roleplay-chat", "player_code_update"));

  public static final PacketCodec<PacketByteBuf, PlayerCodeUpdatePayload> CODEC =
      PacketCodec.of(
          (value, buf) -> {
            buf.writeUuid(value.uuid());
            buf.writeString(value.code());
          },
          buf -> new PlayerCodeUpdatePayload(buf.readUuid(), buf.readString())
      );

  @Override
  public CustomPayload.Id<PlayerCodeUpdatePayload> getId() {
    return ID;
  }
}
