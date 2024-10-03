package net.minecraftfr.roleplaychat.command;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.ChatManager;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.MessageType;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SupportMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;

public class RoleplayChatCommands {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(CommandManager.literal(SpeakMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        SpeakMessage speakMessage = new SpeakMessage(message);

        sendMessageFromCommand(speakMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(ShoutMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        ShoutMessage shoutMessage = new ShoutMessage(ShoutMessage.CHARACTER + message);

        sendMessageFromCommand(shoutMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(WhisperMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        WhisperMessage whisperMessage = new WhisperMessage(WhisperMessage.CHARACTERS[0] + message);

        sendMessageFromCommand(whisperMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(ActionMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        ActionMessage actionMessage = new ActionMessage(ActionMessage.CHARACTER + message);

        sendMessageFromCommand(actionMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(GlobalOOCMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        GlobalOOCMessage globalOOCMessage = new GlobalOOCMessage(GlobalOOCMessage.CHARACTER + message);

        sendMessageFromCommand(globalOOCMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(OOCMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        OOCMessage oOCMessage = new OOCMessage(OOCMessage.CHARACTER + message);

        sendMessageFromCommand(oOCMessage, context);

        return 1;
      }))
    );

    dispatcher.register(CommandManager.literal(SupportMessage.COMMAND)
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        SupportMessage supportMessage = new SupportMessage(SupportMessage.CHARACTER + message);

        sendMessageFromCommand(supportMessage, context);

        return 1;
      }))
    );
  }

  private static void sendMessageFromCommand(MessageType messageType, CommandContext<ServerCommandSource> context) {
    List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
    ServerPlayerEntity sender = players.get(0);

    if (!players.isEmpty()) {
      ChatManager.sendMessageToPlayerListFromPosition(
        sender,
        players,
        messageType
      );
    }
  }
}
