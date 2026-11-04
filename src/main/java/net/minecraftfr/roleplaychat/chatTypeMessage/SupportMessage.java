package net.minecraftfr.roleplaychat.chatTypeMessage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftfr.roleplaychat.config.MessageTypeSettings;

public class SupportMessage extends MessageType {
  public static final String COMMAND = "support";

  public SupportMessage(String message, MessageTypeSettings settings) {
    super(message, settings.radius(), settings.colorRgb(), settings.firstPrefix());
    this.prefixLength = settings.prefixLengthFor(message);
  }

  @Override
  public void sendMessage(ServerPlayer sender, ServerPlayer receiver) {
    if (canReceive(sender, receiver)) {
      super.sendMessage(sender, receiver);
    }
  }

  /*
   * Return if receiver can receipt the message
   * only if the receiver is OP (admin) or if the sender is the receiver
   */
  public boolean canReceive(ServerPlayer sender, ServerPlayer receiver) {
    return receiver.permissions().hasPermission(new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS))
      || sender.getUUID().equals(receiver.getUUID());
  }
}
