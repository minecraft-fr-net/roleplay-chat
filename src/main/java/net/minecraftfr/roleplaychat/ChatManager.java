package net.minecraftfr.roleplaychat;

import java.util.Arrays;
import java.util.List;

import org.joml.Math;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.MessageType;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;

public class ChatManager {
  public boolean handleChatMessage(ServerPlayerEntity player, String message) {
    List<MessageType> messageTypes = Arrays.asList(
      new ShoutMessage(message),
      new WhisperMessage(message),
      new ActionMessage(message),
      new OOCMessage(message),
      new GlobalOOCMessage(message)
    );

    for (MessageType type : messageTypes) {
      if (type.canBeSend()) {
        this.sendLocalMessage(player, type);
        return false;
      }
    }

    // Default message type
    SpeakMessage speakMessage = new SpeakMessage(message);
    this.sendLocalMessage(player, speakMessage);
    return false;
  }

  /**
   * Send message to players within a certain radius in the specified color
   * Send to all players if radius is 0
   */
  private void sendLocalMessage(ServerPlayerEntity player, MessageType messageType) {
    sendMessageToPlayerListFromPosition(
      player,
      player.getServerWorld().getPlayers(),
      messageType
    );
  }

  public static void sendMessageToPlayerListFromPosition(ServerPlayerEntity sender, List<ServerPlayerEntity> players,  MessageType messageType) {
    int radius = messageType.getRadius();

    players.forEach(otherPlayer -> {
      int distance = (int) Math.round(sender.getPos().distanceTo(otherPlayer.getPos()));

      if (radius == 0 || distance <= radius) {
        messageType.setDistance(distance);
        messageType.sendMessage(sender, otherPlayer);
      }
    });
  }
}
