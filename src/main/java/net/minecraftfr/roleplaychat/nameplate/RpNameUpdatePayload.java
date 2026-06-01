package net.minecraftfr.roleplaychat.nameplate;

import java.util.UUID;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Paquet serveur → client diffusé quand un joueur définit (ou change) son pseudo RP.
 * Envoyé aussi en masse au moment de la connexion d'un nouveau joueur pour
 * synchroniser tous les pseudos RP existants.
 */
public record RpNameUpdatePayload(UUID uuid, String rpName) implements CustomPayload {

  public static final CustomPayload.Id<RpNameUpdatePayload> ID =
      new CustomPayload.Id<>(Identifier.of("roleplay-chat", "rp_name_update"));

  public static final PacketCodec<PacketByteBuf, RpNameUpdatePayload> CODEC =
      PacketCodec.of(
          (value, buf) -> {
            buf.writeUuid(value.uuid());
            buf.writeString(value.rpName());
          },
          buf -> new RpNameUpdatePayload(buf.readUuid(), buf.readString())
      );

  @Override
  public CustomPayload.Id<RpNameUpdatePayload> getId() {
    return ID;
  }
}
