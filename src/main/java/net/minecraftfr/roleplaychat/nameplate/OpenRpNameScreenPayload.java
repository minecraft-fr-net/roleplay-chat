package net.minecraftfr.roleplaychat.nameplate;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet serveur → client demandant au client d'ouvrir l'écran de saisie du pseudo RP.
 * Envoyé à la connexion si le joueur n'a pas encore de pseudo, ou en cas d'erreur de validation.
 * {@code errorKey} vide = pas d'erreur. {@code errorArg} = argument de la traduction (peut être vide).
 */
public record OpenRpNameScreenPayload(String errorKey, String errorArg) implements CustomPacketPayload {

  public static final CustomPacketPayload.Type<OpenRpNameScreenPayload> ID =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("roleplay-chat", "open_rp_name_screen"));

  public static final StreamCodec<FriendlyByteBuf, OpenRpNameScreenPayload> CODEC =
      StreamCodec.ofMember(
          (value, buf) -> {
            buf.writeUtf(value.errorKey());
            buf.writeUtf(value.errorArg());
          },
          buf -> new OpenRpNameScreenPayload(buf.readUtf(), buf.readUtf())
      );

  @Override
  public CustomPacketPayload.Type<OpenRpNameScreenPayload> type() {
    return ID;
  }
}
