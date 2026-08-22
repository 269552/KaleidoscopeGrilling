package cn.breezeth.kaleidoscope_grilling.network;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;

import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackMenu;
import cn.breezeth.kaleidoscope_grilling.client.ClientSkewerEatingSound;
import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodMerge;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;

import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.GrillingWirelessIOModePayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Network actions that cannot safely use vanilla container button ids. */
public final class GrillingNetwork {
  private static final String PROTOCOL = "3";
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
    CHANNEL.messageBuilder(ThreadingRecipeSyncPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
        .encoder(ThreadingRecipeSyncPacket::encode)
        .decoder(ThreadingRecipeSyncPacket::decode)
        .consumerMainThread(ThreadingRecipeSyncPacket::handle)
        .add();
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
        PacketDistributor.PLAYER.with(() -> player),
        new SkewerEatingSoundPacket(player.getId(), playing, profile));
  }

  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer player) sendThreadingRecipes(player);
  }

  public static void broadcastThreadingRecipes() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null) return;
    ThreadingRecipeSyncPacket packet =
        new ThreadingRecipeSyncPacket(GrillingDataManager.scriptSkewerRecipes());
    for (ServerPlayer player : server.getPlayerList().getPlayers())
      CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
  }

  private static void sendThreadingRecipes(ServerPlayer player) {
    CHANNEL.send(
        PacketDistributor.PLAYER.with(() -> player),
        new ThreadingRecipeSyncPacket(GrillingDataManager.scriptSkewerRecipes()));
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

  private record ThreadingRecipeSyncPacket(
      List<GrillingDataManager.ScriptSkewerRecipe> recipes) {
    private static final int MAX_RECIPES = 4096;
    private static final int MAX_SELECTORS_PER_SLOT = 256;

    private static void encode(ThreadingRecipeSyncPacket packet, FriendlyByteBuf buffer) {
      buffer.writeVarInt(packet.recipes().size());
      for (var recipe : packet.recipes()) {
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

    private static ThreadingRecipeSyncPacket decode(FriendlyByteBuf buffer) {
      int recipeCount = buffer.readVarInt();
      if (recipeCount < 0 || recipeCount > MAX_RECIPES)
        throw new IllegalArgumentException("Invalid KubeJS threading recipe count: " + recipeCount);
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
            throw new IllegalArgumentException("Invalid threading selector count: " + selectorCount);
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
      return new ThreadingRecipeSyncPacket(List.copyOf(recipes));
    }

    private static void handle(
        ThreadingRecipeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
      contextSupplier.get().setPacketHandled(true);
      DistExecutor.unsafeRunWhenOn(
          Dist.CLIENT,
          () ->
              () -> {
                GrillingDataManager.replaceSyncedSkewerRecipes(packet.recipes());
                cn.breezeth.kaleidoscope_grilling.client.ClientSetup
                    .refreshScriptSkewerRendering();
                if (ModList.get().isLoaded("jei"))
                  cn.breezeth.kaleidoscope_grilling.jei.GrillingJeiPlugin
                      .refreshThreadingRecipes();
              });
    }
  }

  private GrillingNetwork() {}
}
