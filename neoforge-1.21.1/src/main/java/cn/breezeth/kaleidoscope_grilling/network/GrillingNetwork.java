package cn.breezeth.kaleidoscope_grilling.network;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.client.ClientSkewerEatingSound;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;


import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class GrillingNetwork {
  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("1")
        .playToClient(
            SkewerEatingSoundPayload.TYPE,
            SkewerEatingSoundPayload.STREAM_CODEC,
            SkewerEatingSoundPayload::handle);
  }

  public static void setSkewerEatingSound(
      ServerPlayer player, MultiBiteSkewerItem.AnimationProfile profile, boolean playing) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(
        player, new SkewerEatingSoundPayload(player.getId(), playing, profile));
  }

  public record SkewerEatingSoundPayload(
      int entityId, boolean playing, MultiBiteSkewerItem.AnimationProfile profile)
      implements CustomPacketPayload {
    public static final Type<SkewerEatingSoundPayload> TYPE =
        new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "skewer_eating_sound"));
    public static final StreamCodec<ByteBuf, SkewerEatingSoundPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SkewerEatingSoundPayload::entityId,
            ByteBufCodecs.BOOL,
            SkewerEatingSoundPayload::playing,
            ByteBufCodecs.VAR_INT,
            payload -> payload.profile().ordinal(),
            (entityId, playing, profileId) ->
                new SkewerEatingSoundPayload(
                    entityId,
                    playing,
                    MultiBiteSkewerItem.AnimationProfile.values()[
                        Math.max(
                            0,
                            Math.min(
                                profileId,
                                MultiBiteSkewerItem.AnimationProfile.values().length - 1))]));

    public static void handle(SkewerEatingSoundPayload payload, IPayloadContext context) {
      context.enqueueWork(
          () ->
              ClientSkewerEatingSound.handle(
                  payload.entityId(), payload.playing(), payload.profile()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  private GrillingNetwork() {}
}
