package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public abstract class MessageType {
  protected int radius;

  public MessageType(int radius) {
    this.radius = radius;
  }

  public int getRadius() {
    return radius;
  }

  // Méthodes abstraites à implémenter dans les sous-classes
  public abstract boolean canBeSend(String message);
  public abstract MutableText formatMessage(ServerPlayerEntity player, String message);
  public abstract String formatContentMessage(ServerPlayerEntity player, String message);
}
