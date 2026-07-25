package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

/** Network actions that cannot safely use vanilla container button ids. */
public final class GrillingNetwork {
  private static final String PROTOCOL = "1";
  private static final SimpleChannel CHANNEL =
      NetworkRegistry.ChannelBuilder.named(
              new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "main"))
          .networkProtocolVersion(() -> PROTOCOL)
          .clientAcceptedVersions(PROTOCOL::equals)
          .serverAcceptedVersions(PROTOCOL::equals)
          .simpleChannel();

  public static void register() {
    CHANNEL.messageBuilder(HotFoodMergePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
        .encoder((packet, buffer) -> buffer.writeVarInt(packet.slotId()))
        .decoder(buffer -> new HotFoodMergePacket(buffer.readVarInt()))
        .consumerMainThread(HotFoodMergePacket::handle)
        .add();
  }

  public static void requestHotFoodMerge(int slotId) {
    CHANNEL.sendToServer(new HotFoodMergePacket(slotId));
  }

  private record HotFoodMergePacket(int slotId) {
    private static void handle(HotFoodMergePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
      NetworkEvent.Context context = contextSupplier.get();
      context.setPacketHandled(true);
      ServerPlayer player = context.getSender();
      if (player == null) return;
      AbstractContainerMenu menu = player.containerMenu;
      if (menu instanceof AdvancedRackMenu
          || packet.slotId < 0
          || packet.slotId >= menu.slots.size()) return;
      Slot slot = menu.slots.get(packet.slotId);
      int merged = FoodState.mergeHot(slot.getItem(), menu.getCarried(), player.level());
      if (merged <= 0) return;
      slot.setChanged();
      menu.setCarried(menu.getCarried());
      menu.broadcastFullState();
    }
  }

  private GrillingNetwork() {}
}
