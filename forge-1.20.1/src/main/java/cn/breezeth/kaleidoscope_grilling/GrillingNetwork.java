package cn.breezeth.kaleidoscope_grilling;


import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.GrillingWirelessIOModePayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
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
        .encoder(
            (packet, buffer) -> {
              buffer.writeVarInt(packet.containerId());
              buffer.writeVarInt(packet.slotId());
            })
        .decoder(buffer -> new HotFoodMergePacket(buffer.readVarInt(), buffer.readVarInt()))
        .consumerMainThread(HotFoodMergePacket::handle)
        .add();
    CHANNEL.messageBuilder(SkewerEatingSoundPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(
            (packet, buffer) -> {
              buffer.writeVarInt(packet.entityId());
              buffer.writeBoolean(packet.playing());
              buffer.writeEnum(packet.profile());
            })
        .decoder(
            buffer ->
                new SkewerEatingSoundPacket(
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readEnum(MultiBiteSkewerItem.AnimationProfile.class)))
        .consumerMainThread(SkewerEatingSoundPacket::handle)
        .add();
    if (ModList.get().isLoaded("touhou_little_maid")) {
      CHANNEL.messageBuilder(GrillingWirelessIOModePayload.class, 2, NetworkDirection.PLAY_TO_SERVER)
          .encoder(GrillingWirelessIOModePayload::encode)
          .decoder(GrillingWirelessIOModePayload::decode)
          .consumerMainThread(GrillingWirelessIOModePayload::handle)
          .add();
    }
  }

  public static void requestHotFoodMerge(int containerId, int slotId) {
    CHANNEL.sendToServer(new HotFoodMergePacket(containerId, slotId));
  }

  public static void setGrillingWirelessIOMode(boolean enabled) {
    CHANNEL.sendToServer(new GrillingWirelessIOModePayload(enabled));
  }

  public static void setSkewerEatingSound(
      ServerPlayer player, MultiBiteSkewerItem.AnimationProfile profile, boolean playing) {
    CHANNEL.send(
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
        new SkewerEatingSoundPacket(player.getId(), playing, profile));
  }

  private record HotFoodMergePacket(int containerId, int slotId) {
    private static void handle(HotFoodMergePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
      NetworkEvent.Context context = contextSupplier.get();
      context.setPacketHandled(true);
      ServerPlayer player = context.getSender();
      if (player == null) return;
      AbstractContainerMenu menu = player.containerMenu;
      if (menu.containerId != packet.containerId
          || menu instanceof AdvancedRackMenu
          || packet.slotId < 0
          || packet.slotId >= menu.slots.size()) return;
      Slot slot = menu.slots.get(packet.slotId);
      if (!HotFoodMerge.isAllowedTarget(player, menu, slot)) return;
      var target = slot.getItem().copy();
      var source = menu.getCarried().copy();
      int merged = FoodState.mergeHot(target, source, player.level());
      if (merged <= 0) return;
      slot.set(target);
      menu.setCarried(source);
      slot.setChanged();
      menu.broadcastFullState();
    }
  }

  private record SkewerEatingSoundPacket(
      int entityId, boolean playing, MultiBiteSkewerItem.AnimationProfile profile) {
    private static void handle(
        SkewerEatingSoundPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
      contextSupplier.get().setPacketHandled(true);
      DistExecutor.unsafeRunWhenOn(
          Dist.CLIENT,
          () ->
              () ->
                  ClientSkewerEatingSound.handle(
                      packet.entityId(), packet.playing(), packet.profile()));
    }
  }

  private GrillingNetwork() {}
}
