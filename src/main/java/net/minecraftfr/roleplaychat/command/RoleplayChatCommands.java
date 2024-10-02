package net.minecraftfr.roleplaychat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftfr.roleplaychat.ChatManager;
import net.minecraftfr.roleplaychat.chatTypeMessage.MessageType;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraft.util.math.Vec3d;

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
  }

  private static void sendMessageFromCommand(MessageType messageType, CommandContext<ServerCommandSource> context) {
    ServerCommandSource source = context.getSource();

    Vec3d position;
    if (source.getEntity() != null) {
      position = source.getEntity().getPos();
    } else {
      position = source.getPosition();
    }

    ChatManager.sendMessageToPlayerListFromPosition(
      position,
      context.getSource().getServer().getPlayerManager().getPlayerList(),
      messageType
    );
  }
}
