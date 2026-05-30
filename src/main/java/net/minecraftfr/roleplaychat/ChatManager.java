package net.minecraftfr.roleplaychat;

import java.util.Arrays;
import java.util.List;

import org.joml.Math;

import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.MessageType;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;
import net.minecraftfr.roleplaychat.config.RoleplayChatConfig;

public class ChatManager {
  public boolean handleChatMessage(ServerPlayerEntity player, String message, SignedMessage signedMessage) {
    RoleplayChatConfig cfg = RoleplayChatConfig.get();
    List<MessageType> messageTypes = Arrays.asList(
      new ShoutMessage(message, cfg.shout()),
      new WhisperMessage(message, cfg.whisper()),
      new ActionMessage(message, cfg.action()),
      new OOCMessage(message, cfg.ooc()),
      new GlobalOOCMessage(message, cfg.globalOoc())
    );

    for (MessageType type : messageTypes) {
      if (type.canBeSend()) {
        this.sendLocalMessage(player, type, signedMessage);
        return false;
      }
    }

    // Default message type
    SpeakMessage speakMessage = new SpeakMessage(message, cfg.speak());
    this.sendLocalMessage(player, speakMessage, signedMessage);
    return false;
  }

  /**
   * Send message to players within a certain radius in the specified color
   * Send to all players if radius is 0
   */
  private void sendLocalMessage(ServerPlayerEntity player, MessageType messageType, SignedMessage signedMessage) {
    sendMessageToPlayerListFromPosition(
      player,
      ((ServerWorld) player.getWorld()).getPlayers(p -> true),
      messageType,
      signedMessage
    );
  }

  public static void sendMessageToPlayerListFromPosition(ServerPlayerEntity sender, List<ServerPlayerEntity> players,
                                                         MessageType messageType, SignedMessage signedMessage) {
    int radius = messageType.getRadius();

    players.forEach(otherPlayer -> {
      int distance = (int) Math.round(sender.distanceTo(otherPlayer));

      if (radius == 0 || distance <= radius) {
        messageType.setDistance(distance);
        messageType.sendMessage(sender, otherPlayer, signedMessage);
      }
    });
  }
}
