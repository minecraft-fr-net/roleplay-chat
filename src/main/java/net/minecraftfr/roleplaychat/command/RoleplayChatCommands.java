package net.minecraftfr.roleplaychat.command;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
import net.minecraftfr.roleplaychat.nameplate.PlayerCodeStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameRevealPayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameStore;
import net.minecraftfr.roleplaychat.nameplate.RpNameUpdatePayload;
import net.minecraftfr.roleplaychat.nameplate.RpNameVisibilityStore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

public class RoleplayChatCommands {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    registerRpNameCommand(dispatcher);
    registerRpPresentCommand(dispatcher);

    dispatcher.register(Commands.literal("roleplaychat")
      .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
      .then(Commands.literal("reload")
        .executes(context -> {
          if (RoleplayChatConfig.reload()) {
            context.getSource().sendSuccess(() -> Component.literal("[roleplay-chat] Configuration reloaded."), true);
            return 1;
          }
          context.getSource().sendFailure(Component.literal("[roleplay-chat] Reload failed; see server log."));
          return 0;
        })));

    dispatcher.register(Commands.literal(SpeakMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        SpeakMessage speakMessage = new SpeakMessage(message, RoleplayChatConfig.get().speak());

        sendMessageFromCommand(speakMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(ShoutMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().shout();

        ShoutMessage shoutMessage = new ShoutMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(shoutMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(WhisperMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().whisper();

        WhisperMessage whisperMessage = new WhisperMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(whisperMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(ActionMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().action();

        ActionMessage actionMessage = new ActionMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(actionMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(GlobalOOCMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().globalOoc();

        GlobalOOCMessage globalOOCMessage = new GlobalOOCMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(globalOOCMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(OOCMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().ooc();

        OOCMessage oOCMessage = new OOCMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(oOCMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(SupportMessage.COMMAND)
      .then(Commands.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");
        var settings = RoleplayChatConfig.get().support();

        SupportMessage supportMessage = new SupportMessage(settings.firstPrefix() + message, settings);

        sendMessageFromCommand(supportMessage, context);

        return 1;
      }))
    );

    dispatcher.register(Commands.literal(RollMessage.COMMAND)
      .executes(ctx -> executeRoll(ctx, "1d20"))
      .then(Commands.argument("notation", StringArgumentType.word())
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
  private static int executeRoll(CommandContext<CommandSourceStack> ctx, String notation) {
    ServerPlayer sender = ctx.getSource().getPlayer();
    if (sender == null) return 0;

    java.util.regex.Matcher m = DICE_PATTERN.matcher(notation);
    if (!m.matches()) {
      ctx.getSource().sendFailure(Component.literal(
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
      ctx.getSource().sendFailure(Component.literal("Notation invalide. Exemples : d20  1d20+3  d6-1"));
      return 0;
    }

    if (sides < 2 || sides > 1000) {
      ctx.getSource().sendFailure(Component.literal("Le nombre de faces doit être entre 2 et 1000."));
      return 0;
    }

    RollMessage roll = new RollMessage(sides, bonus, RoleplayChatConfig.get().roll());
    List<ServerPlayer> players = ctx.getSource().getServer().getPlayerList().getPlayers();
    ChatManager.sendMessageToPlayerListFromPosition(sender, players, roll, null);
    return 1;
  }

  // ---------------------------------------------------------------------------
  // Commande /rp present
  // ---------------------------------------------------------------------------

  static void registerRpPresentCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("rp")
      .then(Commands.literal("present")
        .then(Commands.argument("hex", StringArgumentType.word())
          .executes(ctx -> {
            ServerPlayer sender = ctx.getSource().getPlayer();
            if (sender == null) return 0;
            String hex = StringArgumentType.getString(ctx, "hex").toUpperCase();

            var server = ctx.getSource().getServer();
            RpNameStore rpStore = RpNameStore.get(server);
            PlayerCodeStore codeStore = PlayerCodeStore.get(server);

            String senderRp = rpStore.getRpName(sender.getUUID()).orElse(null);
            if (senderRp == null) {
              ctx.getSource().sendFailure(Component.literal(
                  "Vous n'avez pas de pseudo RP. Utilisez /rpname pour en définir un."));
              return 0;
            }

            UUID targetUuid = codeStore.getUuidByCode(hex);
            if (targetUuid == null) {
              ctx.getSource().sendFailure(Component.literal("Joueur introuvable : " + hex));
              return 0;
            }
            if (targetUuid.equals(sender.getUUID())) {
              ctx.getSource().sendFailure(Component.literal(
                  "Vous ne pouvez pas vous présenter à vous-même."));
              return 0;
            }

            RpNameVisibilityStore visibilityStore = RpNameVisibilityStore.get(server);
            visibilityStore.addVisibility(targetUuid, sender.getUUID());

            ServerPlayer target = server.getPlayerList().getPlayer(targetUuid);
            if (target != null) {
              ServerPlayNetworking.send(target, new RpNameRevealPayload(sender.getUUID()));
              String senderCode = codeStore.getCode(sender.getUUID()).orElse("?");
              target.displayClientMessage(Component.literal(
                  senderRp + " (" + senderCode + ") s'est présenté à vous."), false);
            }

            ctx.getSource().sendSuccess(
                () -> Component.literal("Vous vous êtes présenté à " + hex + "."), false);
            return 1;
          }))));
  }

  // ---------------------------------------------------------------------------
  // Commande /rpname
  // ---------------------------------------------------------------------------

  private static final int RP_NAME_MAX_LENGTH = 32;

  static void registerRpNameCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(Commands.literal("rpname")
      .then(Commands.argument("name", StringArgumentType.greedyString())
        .executes(ctx -> {
          String name = StringArgumentType.getString(ctx, "name").trim();
          ServerPlayer player = ctx.getSource().getPlayer();
          if (player == null) return 0;

          if (name.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("Le pseudo RP ne peut pas être vide."));
            return 0;
          }
          if (name.length() > RP_NAME_MAX_LENGTH) {
            ctx.getSource().sendFailure(Component.literal(
                "Le pseudo RP ne peut pas dépasser " + RP_NAME_MAX_LENGTH + " caractères."));
            return 0;
          }

          var server = ctx.getSource().getServer();
          RpNameStore store = RpNameStore.get(server);

          if (store.isNameTaken(name, player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(
                "Le pseudo « " + name + " » est déjà utilisé par un autre joueur."));
            return 0;
          }

          store.setRpName(player.getUUID(), name);

          // Synchroniser le nouveau nom RP avec tous les clients connectés
          RpNameUpdatePayload payload = new RpNameUpdatePayload(player.getUUID(), name);
          server.getPlayerList().getPlayers().forEach(p ->
              ServerPlayNetworking.send(p, payload));

          // Mettre à jour la liste Tab (PlayerList)
          server.getPlayerList().broadcastAll(
              new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

          ctx.getSource().sendSuccess(
              () -> Component.literal("Pseudo RP défini : " + name), false);
          return 1;
        })));
  }

  private static void sendMessageFromCommand(MessageType messageType, CommandContext<CommandSourceStack> context) {
    ServerPlayer sender = context.getSource().getPlayer();
    if (sender == null) {
      return;
    }
    List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
    ChatManager.sendMessageToPlayerListFromPosition(sender, players, messageType, null);
  }
}
