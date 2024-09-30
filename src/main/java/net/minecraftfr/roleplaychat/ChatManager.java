package net.minecraftfr.roleplaychat;

import java.util.Arrays;
import java.util.List;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
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
      new ShoutMessage(),
      new WhisperMessage(),
      new ActionMessage(),
      new OOCMessage(),
      new GlobalOOCMessage()
    );

    for (MessageType type : messageTypes) {
      if (type.canBeSend(message)) {
        this.sendLocalMessage(player, type.formatMessage(player, message), type.getRadius());
        return false;
      }
    }

    // Default message type
    SpeakMessage speakMessage = new SpeakMessage();
    this.sendLocalMessage(player, speakMessage.formatMessage(player, message), SpeakMessage.RADIUS);
    return false;
  }

  /**
   * Send message to players within a certain radius in the specified color
   * Send to all players if radius is 0
   */
  private void sendLocalMessage(ServerPlayerEntity player, MutableText message, int radius) {
    player.getServerWorld().getPlayers().forEach(otherPlayer -> {
      if (radius == 0 || player.getPos().isInRange(otherPlayer.getPos(), radius)) {
        otherPlayer.sendMessage(message, false);
      }
    });
  }
}
