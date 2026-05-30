package net.minecraftfr.roleplaychat.command;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraftfr.roleplaychat.ChatManager;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.MessageType;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SupportMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;

public class RoleplayChatCommands {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
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
  }

  private static void sendMessageFromCommand(MessageType messageType, CommandContext<ServerCommandSource> context) {
    ServerPlayerEntity sender = context.getSource().getPlayer();
    if (sender == null) {
      return;
    }
    List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
    ChatManager.sendMessageToPlayerListFromPosition(sender, players, messageType);
  }
}
