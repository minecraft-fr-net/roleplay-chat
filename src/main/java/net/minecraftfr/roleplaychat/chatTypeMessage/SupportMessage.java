package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class SupportMessage extends MessageType {
  public static final String COMMAND = "support";

  public SupportMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), settings.firstPrefix());
    this.prefixLength = settings.prefixLengthFor(message);
  }

  @Override
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
    return receiver.hasPermissionLevel(2)
      || sender.getUuid().equals(receiver.getUuid());
  }
}
