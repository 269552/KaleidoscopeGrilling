package cn.breezeth.kaleidoscope_grilling.network;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.client.ClientSkewerEatingSound;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class GrillingNetwork {
  public static void register(RegisterPayloadHandlersEvent event) {
    event
        .registrar("2")
        .playToClient(
            SkewerEatingSoundPayload.TYPE,
            SkewerEatingSoundPayload.STREAM_CODEC,
            SkewerEatingSoundPayload::handle)
        .playToClient(
            ThreadingRecipeSyncPayload.TYPE,
            ThreadingRecipeSyncPayload.STREAM_CODEC,
            ThreadingRecipeSyncPayload::handle);
  }

  public static void setSkewerEatingSound(
      ServerPlayer player, MultiBiteSkewerItem.AnimationProfile profile, boolean playing) {
    PacketDistributor.sendToPlayer(
        player, new SkewerEatingSoundPayload(player.getId(), playing, profile));
  }

  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) sendThreadingRecipes(player);
  }

  public static void broadcastThreadingRecipes() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) return;
    ThreadingRecipeSyncPayload payload =
        new ThreadingRecipeSyncPayload(GrillingDataManager.scriptSkewerRecipes());
    for (ServerPlayer player : server.getPlayerList().getPlayers())
      PacketDistributor.sendToPlayer(player, payload);
  }

  private static void sendThreadingRecipes(ServerPlayer player) {
    PacketDistributor.sendToPlayer(
        player, new ThreadingRecipeSyncPayload(GrillingDataManager.scriptSkewerRecipes()));
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

  public record ThreadingRecipeSyncPayload(
      List<GrillingDataManager.ScriptSkewerRecipe> recipes) implements CustomPacketPayload {
    private static final int MAX_RECIPES = 4096;
    private static final int MAX_SELECTORS_PER_SLOT = 256;
    public static final Type<ThreadingRecipeSyncPayload> TYPE =
        new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "threading_recipe_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ThreadingRecipeSyncPayload>
        STREAM_CODEC =
            new StreamCodec<>() {
              @Override
              public ThreadingRecipeSyncPayload decode(RegistryFriendlyByteBuf buffer) {
                int recipeCount = buffer.readVarInt();
                if (recipeCount < 0 || recipeCount > MAX_RECIPES)
                  throw new IllegalArgumentException(
                      "Invalid KubeJS threading recipe count: " + recipeCount);
                List<GrillingDataManager.ScriptSkewerRecipe> recipes =
                    new ArrayList<>(recipeCount);
                for (int recipeIndex = 0; recipeIndex < recipeCount; recipeIndex++) {
                  String id = buffer.readUtf();
                  String cookedResult = buffer.readUtf();
                  String threadingResult = buffer.readUtf();
                  String effect = buffer.readUtf();
                  int effectSeconds = buffer.readVarInt();
                  String rawModel = buffer.readUtf();
                  String cookedModel = buffer.readUtf();
                  String eatingAnimation = buffer.readUtf();
                  int slotCount = buffer.readVarInt();
                  if (slotCount < 0 || slotCount > 3)
                    throw new IllegalArgumentException("Invalid threading slot count: " + slotCount);
                  List<List<String>> slots = new ArrayList<>(slotCount);
                  for (int slotIndex = 0; slotIndex < slotCount; slotIndex++) {
                    int selectorCount = buffer.readVarInt();
                    if (selectorCount < 1 || selectorCount > MAX_SELECTORS_PER_SLOT)
                      throw new IllegalArgumentException(
                          "Invalid threading selector count: " + selectorCount);
                    List<String> selectors = new ArrayList<>(selectorCount);
                    for (int selectorIndex = 0; selectorIndex < selectorCount; selectorIndex++)
                      selectors.add(buffer.readUtf());
                    slots.add(List.copyOf(selectors));
                  }
                  recipes.add(
                      new GrillingDataManager.ScriptSkewerRecipe(
                          id,
                          List.copyOf(slots),
                          cookedResult,
                          threadingResult,
                          effect,
                          effectSeconds,
                          rawModel,
                          cookedModel,
                          eatingAnimation));
                }
                return new ThreadingRecipeSyncPayload(List.copyOf(recipes));
              }

              @Override
              public void encode(
                  RegistryFriendlyByteBuf buffer, ThreadingRecipeSyncPayload payload) {
                buffer.writeVarInt(payload.recipes().size());
                for (var recipe : payload.recipes()) {
                  buffer.writeUtf(recipe.id());
                  buffer.writeUtf(recipe.cookedResult());
                  buffer.writeUtf(recipe.threadingResult());
                  buffer.writeUtf(recipe.effect());
                  buffer.writeVarInt(recipe.effectSeconds());
                  buffer.writeUtf(recipe.rawModel());
                  buffer.writeUtf(recipe.cookedModel());
                  buffer.writeUtf(recipe.eatingAnimation());
                  buffer.writeVarInt(recipe.ingredients().size());
                  for (List<String> slot : recipe.ingredients()) {
                    buffer.writeVarInt(slot.size());
                    for (String selector : slot) buffer.writeUtf(selector);
                  }
                }
              }
            };

    public static void handle(ThreadingRecipeSyncPayload payload, IPayloadContext context) {
      context.enqueueWork(
          () -> {
            GrillingDataManager.replaceSyncedSkewerRecipes(payload.recipes());
            cn.breezeth.kaleidoscope_grilling.client.ClientSetup.refreshScriptSkewerRendering();
            if (ModList.get().isLoaded("jei"))
              cn.breezeth.kaleidoscope_grilling.jei.GrillingJeiPlugin.refreshThreadingRecipes();
          });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
      return TYPE;
    }
  }

  private GrillingNetwork() {}
}
