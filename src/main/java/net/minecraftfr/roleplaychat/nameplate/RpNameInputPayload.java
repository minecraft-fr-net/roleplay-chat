package net.minecraftfr.roleplaychat.nameplate;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Paquet client → serveur transportant le pseudo RP saisi dans l'écran de connexion.
 */
public record RpNameInputPayload(String name) implements CustomPayload {

  public static final CustomPayload.Id<RpNameInputPayload> ID =
      new CustomPayload.Id<>(Identifier.of("roleplay-chat", "rp_name_input"));

  public static final PacketCodec<PacketByteBuf, RpNameInputPayload> CODEC =
      PacketCodec.of(
          (value, buf) -> buf.writeString(value.name()),
          buf -> new RpNameInputPayload(buf.readString())
      );

  @Override
  public CustomPayload.Id<RpNameInputPayload> getId() {
    return ID;
  }
}
