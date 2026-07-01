package net.minecraftfr.roleplaychat.nameplate;

import java.util.UUID;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Paquet serveur → client envoyé quand un joueur se présente à un autre
 * (via {@code /rp present}).
 *
 * <p>Distinct de {@link RpNameUpdatePayload} : le serveur continue de
 * synchroniser tous les noms RP à tous les clients (pour le mode créatif).
 * Ce payload est un signal d'autorisation : « tu peux maintenant afficher
 * le nom RP de cet UUID ».
 */
public record RpNameRevealPayload(UUID presenterUuid) implements CustomPayload {

    public static final CustomPayload.Id<RpNameRevealPayload> ID =
        new CustomPayload.Id<>(Identifier.of("roleplay-chat", "rp_name_reveal"));

    public static final PacketCodec<PacketByteBuf, RpNameRevealPayload> CODEC =
        PacketCodec.of(
            (value, buf) -> buf.writeUuid(value.presenterUuid()),
            buf -> new RpNameRevealPayload(buf.readUuid())
        );

    @Override
    public CustomPayload.Id<RpNameRevealPayload> getId() {
        return ID;
    }
}
