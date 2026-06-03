package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.message.SentMessage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
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

  public MutableText formatMessage(ServerPlayerEntity player) {
    int fadedColor = getFadedColor();
    TextColor textColor = TextColor.fromRgb(fadedColor);

    // Segment nom (ex. "<Elara>") avec couleur
    MutableText nameSegment = Text.literal(getChatName(player))
        .styled(style -> style.withColor(textColor));

    // Hover MC username si le joueur a un pseudo RP
    net.minecraft.server.MinecraftServer server = player.getServer();
    if (server != null) {
      String rpName = net.minecraftfr.roleplaychat.nameplate.RpNameStore
          .get(server).getRpName(player.getUuid()).orElse(null);
      if (rpName != null) {
        String mcName = player.getName().getString();
        nameSegment.styled(style ->
            style.withHoverEvent(new net.minecraft.text.HoverEvent.ShowText(
                Text.literal(mcName))));
      }
    }

    // Segment contenu (ex. " Bonjour à tous !")
    MutableText contentSegment = Text.literal(" " + contentAfterPrefix())
        .styled(style -> style.withColor(textColor));

    return nameSegment.append(contentSegment);
  }

  public String formatContentMessage(ServerPlayerEntity player) {
    return getChatName(player) + " " + contentAfterPrefix();
  }

  /**
   * Clé du chat type avec décoration pass-through via le champ {@code sender} :
   * {@code parameters: ["sender"], %s} → affiche {@code params.name()} tel quel.
   * Utilisé avec un {@link SignedMessage} signé → aucun indicateur dans le chat.
   * Fallback Profileless si le message n'est pas signé (offline / intégré).
   */
  private static final RegistryKey<net.minecraft.network.message.MessageType> SIGNED_ROLEPLAY_CHAT_TYPE =
      RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.of("roleplay-chat", "signed_roleplay"));

  /**
   * Clé du chat type Profileless utilisé quand aucun {@link SignedMessage} n'est
   * disponible (messages injectés depuis les tests).
   */
  private static final RegistryKey<net.minecraft.network.message.MessageType> ROLEPLAY_CHAT_TYPE =
      RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Identifier.of("roleplay-chat", "roleplay"));

  /**
   * Envoie le message formaté au joueur cible.
   *
   * <p>Si {@code signedMessage} est non-null, utilise {@link SentMessage#of(SignedMessage)}
   * avec le chat type {@code signed_roleplay} (décoration = notre texte pré-formaté via le
   * champ {@code sender}) : le {@link net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket}
   * résultant porte la signature originale du joueur → zéro indicateur côté client.
   *
   * <p>Si {@code signedMessage} est null (messages synthétiques de tests), bascule
   * sur un {@link SentMessage.Profileless} avec le chat type {@code roleplay}.
   *
   * @param sender        joueur qui envoie le message
   * @param receiver      joueur qui reçoit le message
   * @param signedMessage message signé d'origine, ou {@code null} pour les injections de test
   */
  public void sendMessage(ServerPlayerEntity sender, ServerPlayerEntity receiver,
                          @Nullable SignedMessage signedMessage) {
    MutableText formatted = this.formatMessage(sender);

    if (signedMessage != null) {
      // withUnsignedContent : le client affiche notre texte coloré (getContent() retourne
      // directement le unsignedContent). La signature originale reste valide (couvre le
      // contenu brut non modifié). Affiche l'indicateur "modified" côté client.
      SignedMessage modified = signedMessage.withUnsignedContent(formatted);
      var params = net.minecraft.network.message.MessageType.params(
          ROLEPLAY_CHAT_TYPE,
          sender.getRegistryManager(),
          sender.getName()
      );
      SentMessage.of(modified).send(receiver, false, params);
    } else {
      // Pas de message signé disponible (tests) : fallback Profileless
      var params = net.minecraft.network.message.MessageType.params(
          ROLEPLAY_CHAT_TYPE,
          sender.getRegistryManager(),
          sender.getName()
      );
      new SentMessage.Profileless(formatted).send(receiver, false, params);
    }
  }

  /**
   * Surcharge sans {@link SignedMessage} pour les injections de test (Profileless).
   *
   * @deprecated Préférer {@link #sendMessage(ServerPlayerEntity, ServerPlayerEntity, SignedMessage)}
   *             en production pour éviter l'indicateur système.
   */
  public void sendMessage(ServerPlayerEntity sender, ServerPlayerEntity receiver) {
    sendMessage(sender, receiver, null);
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

  /*
   * Return the name of the player for the chat
   * Example : <Jeb_>
   */
  protected String getChatName(ServerPlayerEntity player) {
    net.minecraft.server.MinecraftServer server = player.getServer();
    if (server != null) {
      java.util.Optional<String> rp =
          net.minecraftfr.roleplaychat.nameplate.RpNameStore.get(server)
              .getRpName(player.getUuid());
      if (rp.isPresent()) return "<" + rp.get() + ">";
    }
    return "<" + player.getName().getString() + ">";
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
