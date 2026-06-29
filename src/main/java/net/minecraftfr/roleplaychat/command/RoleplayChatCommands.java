package net.minecraftfr.roleplaychat.command;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.ChatManager;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.MessageType;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.RollMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SupportMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class RoleplayChatCommands {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    registerRpNameCommand(dispatcher);

    dispatcher.register(CommandManager.literal("roleplaychat")
      .requires(CommandManager.requirePermissionLevel(4))
      .then(CommandManager.literal("reload")
        .executes(context -> {
          if (RoleplayChatConfig.reload()) {
            context.getSource().sendFeedback(() -> Text.literal("[roleplay-chat] Configuration reloaded."), true);
            return 1;
          }
          context.getSource().sendError(Text.literal("[roleplay-chat] Reload failed; see server log."));
          return 0;
        })));

    dispatcher.register(CommandManager.literal(SpeakMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        SpeakMessage speakMessage = new SpeakMessage(message, RoleplayChatConfig.get().speak());

        sendMessageFromCommand(speakMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(ShoutMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().shout();

        ShoutMessage shoutMessage = new ShoutMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(shoutMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(WhisperMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().whisper();

        WhisperMessage whisperMessage = new WhisperMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(whisperMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(ActionMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().action();

        ActionMessage actionMessage = new ActionMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(actionMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(GlobalOOCMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().globalOoc();

        GlobalOOCMessage globalOOCMessage = new GlobalOOCMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(globalOOCMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(OOCMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().ooc();

        OOCMessage oOCMessage = new OOCMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(oOCMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(SupportMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().support();

        SupportMessage supportMessage = new SupportMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(supportMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(RollMessage.COMMAND)
      .executes(ctx -> executeRoll(ctx, "1d20"))
      .then(CommandManager.argument("notation", StringArgumentType.word())
        .executes(ctx -> executeRoll(ctx, StringArgumentType.getString(ctx, "notation"))))
    );
  }

  private static final java.util.regex.Pattern DICE_PATTERN =
      java.util.regex.Pattern.compile("^(\\d+)?[dD](\\d+)([+-]\\d+)?$");

  /**
   * Parse et exécute un jet de dé à partir d'une notation du type {@code 1d20+3}.
   * Formats acceptés : {@code d20}, {@code 1d20}, {@code d6-1}, {@code 2d8+2} (le
   * multiplicateur est ignoré — un seul dé est toujours lancé).
   */
  private static int executeRoll(CommandContext<ServerCommandSource> ctx, String notation) {
    ServerPlayerEntity sender = ctx.getSource().getPlayer();
    if (sender == null) return 0;

    java.util.regex.Matcher m = DICE_PATTERN.matcher(notation);
    if (!m.matches()) {
      ctx.getSource().sendError(Text.literal(
          "Notation invalide. Exemples : d20  1d20+3  d6-1"));
      return 0;
    }

    int sides;
    int bonus = 0;
    try {
      sides = Integer.parseInt(m.group(2));
      if (m.group(3) != null) {
        bonus = Integer.parseInt(m.group(3)); // inclut le signe + ou -
      }
    } catch (NumberFormatException e) {
      ctx.getSource().sendError(Text.literal("Notation invalide. Exemples : d20  1d20+3  d6-1"));
      return 0;
    }

    if (sides < 2 || sides > 1000) {
      ctx.getSource().sendError(Text.literal("Le nombre de faces doit être entre 2 et 1000."));
      return 0;
    }

    RollMessage roll = new RollMessage(sides, bonus, RoleplayChatConfig.get().roll());
    List<ServerPlayerEntity> players = ctx.getSource().getServer().getPlayerManager().getPlayerList();
    ChatManager.sendMessageToPlayerListFromPosition(sender, players, roll, null);
    return 1;
  }

  // ---------------------------------------------------------------------------
  // Commande /rpname
  // ---------------------------------------------------------------------------

  private static final int RP_NAME_MAX_LENGTH = 32;

  static void registerRpNameCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(CommandManager.literal("rpname")
      .then(CommandManager.literal("set")
        .requires(source -> source.hasPermissionLevel(2))
        .then(CommandManager.argument("player", EntityArgumentType.player())
          .then(CommandManager.argument("name", StringArgumentType.greedyString())
            .executes(ctx -> {
              String name = StringArgumentType.getString(ctx, "name").trim();
              ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");

              if (name.isEmpty()) {
                ctx.getSource().sendError(Text.literal("Le pseudo RP ne peut pas être vide."));
                return 0;
              }
              if (name.length() > RP_NAME_MAX_LENGTH) {
                ctx.getSource().sendError(Text.literal(
                    "Le pseudo RP ne peut pas dépasser " + RP_NAME_MAX_LENGTH + " caractères."));
                return 0;
              }

              var server = ctx.getSource().getServer();
              RpNameStore store = RpNameStore.get(server);

              if (store.isNameTaken(name, target.getUuid())) {
                ctx.getSource().sendError(Text.literal(
                    "Le pseudo « " + name + " » est déjà utilisé par un autre joueur."));
                return 0;
              }

              store.setRpName(target.getUuid(), name);

              RpNameUpdatePayload payload = new RpNameUpdatePayload(target.getUuid(), name);
              server.getPlayerManager().getPlayerList().forEach(p ->
                  ServerPlayNetworking.send(p, payload));

              server.getPlayerManager().sendToAll(
                  new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, target));

              ctx.getSource().sendFeedback(
                  () -> Text.literal("Pseudo RP de " + target.getName().getString() + " défini : " + name), true);
              target.sendMessage(Text.literal("Votre pseudo RP a été défini en « " + name + " » par un administrateur."));
              return 1;
            }))))
      .then(CommandManager.argument("name", StringArgumentType.greedyString())
        .executes(ctx -> {
          String name = StringArgumentType.getString(ctx, "name").trim();
          ServerPlayerEntity player = ctx.getSource().getPlayer();
          if (player == null) return 0;

          if (name.isEmpty()) {
            ctx.getSource().sendError(Text.literal("Le pseudo RP ne peut pas être vide."));
            return 0;
          }
          if (name.length() > RP_NAME_MAX_LENGTH) {
            ctx.getSource().sendError(Text.literal(
                "Le pseudo RP ne peut pas dépasser " + RP_NAME_MAX_LENGTH + " caractères."));
            return 0;
          }

          var server = ctx.getSource().getServer();
          RpNameStore store = RpNameStore.get(server);

          if (store.isNameTaken(name, player.getUuid())) {
            ctx.getSource().sendError(Text.literal(
                "Le pseudo « " + name + " » est déjà utilisé par un autre joueur."));
            return 0;
          }

          store.setRpName(player.getUuid(), name);

          // Synchroniser le nouveau nom RP avec tous les clients connectés
          RpNameUpdatePayload payload = new RpNameUpdatePayload(player.getUuid(), name);
          server.getPlayerManager().getPlayerList().forEach(p ->
              ServerPlayNetworking.send(p, payload));

          // Mettre à jour la liste Tab (PlayerList)
          server.getPlayerManager().sendToAll(
              new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));

          ctx.getSource().sendFeedback(
              () -> Text.literal("Pseudo RP défini : " + name), false);
          return 1;
        })));
  }

  private static void sendMessageFromCommand(MessageType messageType, CommandContext<ServerCommandSource> context) {
    ServerPlayerEntity sender = context.getSource().getPlayer();
    if (sender == null) {
      return;
    }
    List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
    ChatManager.sendMessageToPlayerListFromPosition(sender, players, messageType, null);
  }
}
