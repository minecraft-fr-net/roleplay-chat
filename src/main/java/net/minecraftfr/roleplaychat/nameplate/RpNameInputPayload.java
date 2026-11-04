package net.minecraftfr.roleplaychat.nameplate;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet client → serveur transportant le pseudo RP saisi dans l'écran de connexion.
 */
public record RpNameInputPayload(String name) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<RpNameInputPayload> ID =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("roleplay-chat", "rp_name_input"));

  public static final StreamCodec<FriendlyByteBuf, RpNameInputPayload> CODEC =
      StreamCodec.ofMember(
          (value, buf) -> buf.writeUtf(value.name()),
          buf -> new RpNameInputPayload(buf.readUtf())
      );

  @Override
  public CustomPacketPayload.Type<RpNameInputPayload> type() {
    return ID;
  }
}
