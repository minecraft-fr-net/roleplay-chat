package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class RollMessage extends MessageType {
  public static final String COMMAND = "roll";

  private final int sides;
  private final int roll;  // résultat brut [1..sides], calculé une fois dans le constructeur
  private final int bonus;

  public RollMessage(int sides, int bonus, MessageTypeSettings settings) {
    super("", settings.radius(), settings.colorRgb(), null);
    this.sides = sides;
    this.bonus = bonus;
    this.roll  = new java.util.Random().nextInt(sides) + 1;
  }


  /** Retourne le résultat brut du dé (1..sides), pour les tests. */
  public int getRoll() {
    return roll;
  }

  @Override
  public boolean canBeSend() {
    return false; // commande uniquement, pas de préfixe chat
  }

  @Override
  public MutableText formatMessage(ServerPlayerEntity player) {
    boolean criticalSuccess = roll == sides;
    boolean criticalFailure = roll == 1;
    boolean critical = criticalSuccess || criticalFailure;

    int color = getFadedColor();

    MutableText text = Text.literal(formatContentMessage(player))
        .styled(style -> critical
            ? style.withColor(TextColor.fromRgb(color)).withBold(true)
            : style.withColor(TextColor.fromRgb(color)));

    if (criticalSuccess) {
      text.append(Text.literal(" "))
          .append(Text.translatable("roleplay-chat.roll.critical_success")
              .styled(style -> style.withColor(TextColor.fromRgb(color)).withBold(true)));
    } else if (criticalFailure) {
      text.append(Text.literal(" "))
          .append(Text.translatable("roleplay-chat.roll.critical_failure")
              .styled(style -> style.withColor(TextColor.fromRgb(color)).withBold(true)));
    }

    return text;
  }

  @Override
  public String formatContentMessage(ServerPlayerEntity player) {
    int total = roll + bonus;
    String name = getPlayerDisplayName(player);
    // Notation affichée : « 1d20+3 » ou « 1d20 » si pas de bonus
    StringBuilder notation = new StringBuilder("1d").append(sides);
    if (bonus > 0) notation.append("+").append(bonus);
    else if (bonus < 0) notation.append(bonus); // "-2" inclut déjà le signe
    // Résultat : « * Alice [1d20+3] → 17 (14+3) » ou « * Alice [1d20] → 14 »
    StringBuilder sb = new StringBuilder("* ")
        .append(name).append(" [").append(notation).append("] → ").append(total);
    if (bonus != 0) {
      sb.append(" (").append(roll).append(bonus > 0 ? "+" : "").append(bonus).append(")");
    }
    return sb.toString();
  }
}
