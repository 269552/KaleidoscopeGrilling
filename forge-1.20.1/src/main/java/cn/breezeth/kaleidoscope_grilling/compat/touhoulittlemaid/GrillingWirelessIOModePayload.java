package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.other.WirelessIOContainer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record GrillingWirelessIOModePayload(boolean enabled) {
  public static void encode(GrillingWirelessIOModePayload payload, FriendlyByteBuf buffer) {
    buffer.writeBoolean(payload.enabled());
  }

  public static GrillingWirelessIOModePayload decode(FriendlyByteBuf buffer) {
    return new GrillingWirelessIOModePayload(buffer.readBoolean());
  }

  public static void handle(
      GrillingWirelessIOModePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    context.enqueueWork(
        () -> {
          if (context.getSender() != null
              && context.getSender().containerMenu instanceof WirelessIOContainer menu)
            GrillingWirelessIOData.setEnabled(menu.getWirelessIO(), payload.enabled());
        });
    context.setPacketHandled(true);
  }
}
