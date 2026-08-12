package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.other.WirelessIOContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GrillingWirelessIOModePayload(boolean enabled) implements CustomPacketPayload {
  public static final Type<GrillingWirelessIOModePayload> TYPE =
      new Type<>(
          ResourceLocation.fromNamespaceAndPath(
              KaleidoscopeGrilling.MOD_ID, "grilling_wireless_io_mode"));
  public static final StreamCodec<ByteBuf, GrillingWirelessIOModePayload> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.BOOL,
          GrillingWirelessIOModePayload::enabled,
          GrillingWirelessIOModePayload::new);

  public static void handle(GrillingWirelessIOModePayload payload, IPayloadContext context) {
    context.enqueueWork(
        () -> {
          if (context.player().containerMenu instanceof WirelessIOContainer menu)
            GrillingWirelessIOData.setEnabled(menu.getWirelessIO(), payload.enabled());
        });
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
