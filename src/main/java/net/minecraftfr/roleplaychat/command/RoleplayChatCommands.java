package net.minecraftfr.roleplaychat.command;
import org.joml.Math;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RoleplayChatCommands {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(CommandManager.literal("sendroleplaymessage")
      .then(CommandManager.argument("player", EntityArgumentType.player())
      .then(CommandManager.argument("message", StringArgumentType.string())
      .executes(context -> {
        String message = StringArgumentType.getString(context, "message");

        SpeakMessage speakMessage = new SpeakMessage(message);

        ServerCommandSource source = context.getSource();

        Vec3d position;
        if (source.getEntity() != null) {
          position = source.getEntity().getPos();
        } else {
          position = source.getPosition();
        }

        int radius = speakMessage.getRadius();
        for (ServerPlayerEntity otherPlayer : context.getSource().getServer().getPlayerManager().getPlayerList()) {
          int distance = (int) Math.round(position.distanceTo(otherPlayer.getPos()));
    
          if (radius == 0 || distance <= radius) {
            speakMessage.setDistance(distance);
            speakMessage.sendMessage(otherPlayer);
          }
        };

        return 1;
      }))));
  }
}
