package net.minecraftfr.roleplaychat.chatTypeMessage;

import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.server.network.ServerPlayerEntity;

public class SupportMessage extends MessageType {
  public static final int RADIUS = 0;
  public static final int COLOR = 0xFF99CC;
  public static final String CHARACTER = "?";
  public static final String COMMAND = "support";

  public SupportMessage(String message) {
    super(message, RADIUS, COLOR, CHARACTER);
  }

  @Overwrite
  public void sendMessage(ServerPlayerEntity sender, ServerPlayerEntity receiver) {
    if (canReceive(sender, receiver)) {
      super.sendMessage(sender, receiver);
    }
  }

  /*
   * Return if receiver can receipt the message
   * only if the receiver is OP (admin) or if the sender is the receiver
   */
  public boolean canReceive(ServerPlayerEntity sender, ServerPlayerEntity receiver) {
    return receiver.hasPermissionLevel(2) || 
      sender.getUuid() == receiver.getUuid();
  }
}
