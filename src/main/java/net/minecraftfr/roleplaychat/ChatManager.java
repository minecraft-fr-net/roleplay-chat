package net.minecraftfr.roleplaychat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraftfr.roleplaychat.chatTypeMessage.ActionMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.GlobalOOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.OOCMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.ShoutMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.SpeakMessage;
import net.minecraftfr.roleplaychat.chatTypeMessage.WhisperMessage;

public class ChatManager {
  public boolean handleChatMessage(ServerPlayerEntity player, String message) {
    if (ShoutMessage.canBeSend(message)) {
      this.sendLocalMessage(player, ShoutMessage.formatMessage(player, message), ShoutMessage.RADIUS);
      return false;
    }
    if (WhisperMessage.canBeSend(message)) {
      this.sendLocalMessage(player, WhisperMessage.formatMessage(player, message), WhisperMessage.RADIUS);
      return false;
    }
    if (ActionMessage.canBeSend(message)) {
      this.sendLocalMessage(player, ActionMessage.formatMessage(player, message), ActionMessage.RADIUS);
      return false;
    }
    if (OOCMessage.canBeSend(message)) {
      this.sendLocalMessage(player, OOCMessage.formatMessage(player, message), OOCMessage.RADIUS);
      return false;
    }
    if (GlobalOOCMessage.canBeSend(message)) {
      this.sendLocalMessage(player, GlobalOOCMessage.formatMessage(player, message), GlobalOOCMessage.RADIUS);
      return false;
    }
    
    this.sendLocalMessage(player, SpeakMessage.formatMessage(player, message), SpeakMessage.RADIUS);
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
