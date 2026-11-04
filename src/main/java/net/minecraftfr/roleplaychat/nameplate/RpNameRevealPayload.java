package net.minecraftfr.roleplaychat.nameplate;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet serveur → client envoyé quand un joueur se présente à un autre
 * (via {@code /rp present}).
 *
 * <p>Distinct de {@link RpNameUpdatePayload} : le serveur continue de
 * synchroniser tous les noms RP à tous les clients (pour le mode créatif).
 * Ce payload est un signal d'autorisation : « tu peux maintenant afficher
 * le nom RP de cet UUID ».
 */
public record RpNameRevealPayload(UUID presenterUuid) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RpNameRevealPayload> ID =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("roleplay-chat", "rp_name_reveal"));

    public static final StreamCodec<FriendlyByteBuf, RpNameRevealPayload> CODEC =
        StreamCodec.ofMember(
            (value, buf) -> buf.writeUUID(value.presenterUuid()),
            buf -> new RpNameRevealPayload(buf.readUUID())
        );

    @Override
    public CustomPacketPayload.Type<RpNameRevealPayload> type() {
        return ID;
    }
}
