package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class MessageType {
  protected String message;
  protected int radius;
  protected int color;
  protected String character;
  protected String command;
  protected Integer distance;
  /** Longueur du préfixe retiré dans {@link #contentAfterPrefix()} (0 = message entier). */
  protected int prefixLength = 1;

  public MessageType(String message, int radius, int color, String character) {
    this.message = message;
    this.radius = radius;
    this.color = color;
    this.character = character;
  }

  protected String contentAfterPrefix() {
    if (prefixLength <= 0) {
      return message.trim();
    }
    if (prefixLength >= message.length()) {
      return "";
    }
    return message.substring(prefixLength).trim();
  }

  public int getRadius() {
    return radius;
  }

  public String getMessage() {
    return message;
  }

  public boolean canBeSend() {
    return character != null && message.startsWith(character);
  }

  public MutableComponent formatMessage(ServerPlayer player) {
    int fadedColor = getFadedColor();
    TextColor textColor = TextColor.fromRgb(fadedColor);

    // Segment nom (ex. "<Elara>") avec couleur
    MutableComponent nameSegment = Component.literal(getChatName(player))
        .withStyle(style -> style.withColor(textColor));

    // Hover code hexadécimal quand le joueur a un pseudo RP (le code n'est pas visible dans le message)
    net.minecraft.server.MinecraftServer server = player.level().getServer();
    if (server != null) {
      String rpName = net.minecraftfr.roleplaychat.nameplate.RpNameStore
          .get(server).getRpName(player.getUUID()).orElse(null);
      if (rpName != null) {
        net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore.get(server)
            .getCode(player.getUUID())
            .ifPresent(code -> nameSegment.withStyle(style ->
                style.withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(
                    Component.literal(code)))));
      }
    }

    // Segment contenu (ex. " Bonjour à tous !")
    MutableComponent contentSegment = Component.literal(" " + contentAfterPrefix())
        .withStyle(style -> style.withColor(textColor));

    return nameSegment.append(contentSegment);
  }

  public String formatContentMessage(ServerPlayer player) {
    return getChatName(player) + " " + contentAfterPrefix();
  }

  /**
   * Clé du chat type avec décoration pass-through via le champ {@code sender} :
   * {@code parameters: ["sender"], %s} → affiche {@code params.name()} tel quel.
   * Utilisé avec un {@link PlayerChatMessage} signé → aucun indicateur dans le chat.
   * Fallback Profileless si le message n'est pas signé (offline / intégré).
   */
  private static final ResourceKey<net.minecraft.network.chat.ChatType> SIGNED_ROLEPLAY_CHAT_TYPE =
      ResourceKey.create(Registries.CHAT_TYPE, Identifier.fromNamespaceAndPath("roleplay-chat", "signed_roleplay"));

  /**
   * Clé du chat type Profileless utilisé quand aucun {@link PlayerChatMessage} n'est
   * disponible (messages injectés depuis les tests).
   */
  private static final ResourceKey<net.minecraft.network.chat.ChatType> ROLEPLAY_CHAT_TYPE =
      ResourceKey.create(Registries.CHAT_TYPE, Identifier.fromNamespaceAndPath("roleplay-chat", "roleplay"));

  /**
   * Envoie le message formaté au joueur cible.
   *
   * <p>Si {@code signedMessage} est non-null, utilise {@link OutgoingChatMessage#create(PlayerChatMessage)}
   * avec le chat type {@code signed_roleplay} (décoration = notre texte pré-formaté via le
   * champ {@code sender}) : le {@link net.minecraft.network.protocol.game.ClientboundPlayerChatPacket}
   * résultant porte la signature originale du joueur → zéro indicateur côté client.
   *
   * <p>Si {@code signedMessage} est null (messages synthétiques de tests), bascule
   * sur un {@link OutgoingChatMessage.Disguised} avec le chat type {@code roleplay}.
   *
   * @param sender        joueur qui envoie le message
   * @param receiver      joueur qui reçoit le message
   * @param signedMessage message signé d'origine, ou {@code null} pour les injections de test
   */
  public void sendMessage(ServerPlayer sender, ServerPlayer receiver,
                          @Nullable PlayerChatMessage signedMessage) {
    MutableComponent formatted = this.formatMessage(sender);

    if (signedMessage != null) {
      // withUnsignedContent : le client affiche notre texte coloré (getContent() retourne
      // directement le unsignedContent). La signature originale reste valide (couvre le
      // contenu brut non modifié). Affiche l'indicateur "modified" côté client.
      PlayerChatMessage modified = signedMessage.withUnsignedContent(formatted);
      var params = net.minecraft.network.chat.ChatType.bind(
          ROLEPLAY_CHAT_TYPE,
          sender.registryAccess(),
          sender.getName()
      );
      OutgoingChatMessage.create(modified).sendToPlayer(receiver, false, params);
    } else {
      // Pas de message signé disponible (tests) : fallback Profileless
      var params = net.minecraft.network.chat.ChatType.bind(
          ROLEPLAY_CHAT_TYPE,
          sender.registryAccess(),
          sender.getName()
      );
      new OutgoingChatMessage.Disguised(formatted).sendToPlayer(receiver, false, params);
    }
  }

  /**
   * Surcharge sans {@link PlayerChatMessage} pour les injections de test (Profileless).
   *
   * @deprecated Préférer {@link #sendMessage(ServerPlayer, ServerPlayer, PlayerChatMessage)}
   *             en production pour éviter l'indicateur système.
   */
  public void sendMessage(ServerPlayer sender, ServerPlayer receiver) {
    sendMessage(sender, receiver, null);
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

  /** Résout le nom visible du joueur : pseudo RP → code hex → username MC. */
  protected String getPlayerDisplayName(ServerPlayer player) {
    net.minecraft.server.MinecraftServer server = player.level().getServer();
    if (server != null) {
      java.util.Optional<String> rp =
          net.minecraftfr.roleplaychat.nameplate.RpNameStore.get(server)
              .getRpName(player.getUUID());
      if (rp.isPresent()) return rp.get();
      java.util.Optional<String> code =
          net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore.get(server)
              .getCode(player.getUUID());
      if (code.isPresent()) return code.get();
    }
    return player.getName().getString();
  }

  /** Retourne le nom du joueur encadré de chevrons, ex. {@code <Elara>}. */
  protected String getChatName(ServerPlayer player) {
    return "<" + getPlayerDisplayName(player) + ">";
  }

  protected int getDistance() {
    return distance;
  }

  /*
   * Return the color for the message with the opacity
   * The opacity is defined from the distance between the two players
   */
  protected int getFadedColor() {
    if (distance == null) {
      return this.color;
    }

    int maxDistance = radius;
    if (maxDistance <= 0) {
      return this.color;
    }

    // Calcul du facteur de fade entre 0 (proche) et 1 (loin)
    float fadeFactor = Math.min((float)distance / (float)maxDistance, 1.0f);

    // Calcul des nouvelles composantes RGB basées sur le fadeFactor
    int red = (int) ((color >> 16 & 0xFF) * (1.0 - fadeFactor));
    int green = (int) ((color >> 8 & 0xFF) * (1.0 - fadeFactor));
    int blue = (int) ((color & 0xFF) * (1.0 - fadeFactor));

    // Combiner les composantes pour obtenir la nouvelle couleur
    int fadedColor = (red << 16) | (green << 8) | blue;

    return fadedColor;
  }
}
