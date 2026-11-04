package net.minecraftfr.roleplaychat.nameplate;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet serveur → client diffusé quand un joueur définit (ou change) son pseudo RP.
 * Envoyé aussi en masse au moment de la connexion d'un nouveau joueur pour
 * synchroniser tous les pseudos RP existants.
 */
public record RpNameUpdatePayload(UUID uuid, String rpName) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<RpNameUpdatePayload> ID =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("roleplay-chat", "rp_name_update"));

  public static final StreamCodec<FriendlyByteBuf, RpNameUpdatePayload> CODEC =
      StreamCodec.ofMember(
          (value, buf) -> {
            buf.writeUUID(value.uuid());
            buf.writeUtf(value.rpName());
          },
          buf -> new RpNameUpdatePayload(buf.readUUID(), buf.readUtf())
      );

  @Override
  public CustomPacketPayload.Type<RpNameUpdatePayload> type() {
    return ID;
  }
}
