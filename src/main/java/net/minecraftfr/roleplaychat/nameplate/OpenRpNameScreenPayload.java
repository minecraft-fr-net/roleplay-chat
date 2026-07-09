package net.minecraftfr.roleplaychat.nameplate;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Paquet serveur → client demandant au client d'ouvrir l'écran de saisie du pseudo RP.
 * Envoyé à la connexion si le joueur n'a pas encore de pseudo, ou en cas d'erreur de validation.
 * {@code errorKey} vide = pas d'erreur. {@code errorArg} = argument de la traduction (peut être vide).
 */
public record OpenRpNameScreenPayload(String errorKey, String errorArg) implements CustomPayload {

  public static final CustomPayload.Id<OpenRpNameScreenPayload> ID =
      new CustomPayload.Id<>(Identifier.of("roleplay-chat", "open_rp_name_screen"));

  public static final PacketCodec<PacketByteBuf, OpenRpNameScreenPayload> CODEC =
      PacketCodec.of(
          (value, buf) -> {
            buf.writeString(value.errorKey());
            buf.writeString(value.errorArg());
          },
          buf -> new OpenRpNameScreenPayload(buf.readString(), buf.readString())
      );

  @Override
  public CustomPayload.Id<OpenRpNameScreenPayload> getId() {
    return ID;
  }
}
