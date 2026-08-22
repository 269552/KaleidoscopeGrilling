package cn.breezeth.kaleidoscope_grilling.client;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModFluids;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModMenus;

import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackRenderer;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackScreen;
import cn.breezeth.kaleidoscope_grilling.oil.AnvilPressAnimationAccess;
import cn.breezeth.kaleidoscope_grilling.oil.BigVatRenderer;
import cn.breezeth.kaleidoscope_grilling.skewer.FailedSkewerData;
import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import cn.breezeth.kaleidoscope_grilling.grill.GrillRenderer;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.rack.RackSelectionScreen;
import cn.breezeth.kaleidoscope_grilling.rack.RackShortcutScreen;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningBottleRenderer;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningColorProvider;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import cn.breezeth.kaleidoscope_grilling.skewer.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerColorProvider;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerGuiDecorator;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerGuiIconCache;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerOutlineRender;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerPlateRenderer;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipeBlockRenderer;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(
    modid = KaleidoscopeGrilling.MOD_ID,
    bus = EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT)
public final class ClientSetup {
  private static final ResourceLocation SKEWER_STATE =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "skewer_state");
  private static final ResourceLocation SKEWER_OUTLINE =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "skewer_outline");
  private static final ResourceLocation SKEWER_HOT =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "skewer_hot");
  private static final ResourceLocation SKEWER_COOKING_STAGE =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "skewer_cooking_stage");
  private static final ResourceLocation SKEWER_BITE_STAGE =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "skewer_bite_stage");
  private static final ResourceLocation SLIME_SKEWER_FRAME =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "slime_skewer_frame");
  private static final ResourceLocation FAILED_SKEWER_SOURCE =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "failed_skewer_source");
  private static final ResourceLocation OIL_TYPE =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "oil_type");
  private static final ResourceLocation OIL_BRUSHING =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "oil_brushing");
  private static final ResourceLocation SEASONING_FILL =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "seasoning_fill");
  private static final ResourceLocation SEASONING_REMAINING =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "seasoning_remaining");
  private static final ResourceLocation SEASONING_VARIANT =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "seasoning_variant");

  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          BlockEntityRenderers.register(ModBlockEntities.GRILL.get(), GrillRenderer::new);
          BlockEntityRenderers.register(
              ModBlockEntities.SEASONING_BOTTLE.get(), SeasoningBottleRenderer::new);
          BlockEntityRenderers.register(ModBlockEntities.BIG_VAT.get(), BigVatRenderer::new);
          BlockEntityRenderers.register(
              ModBlockEntities.ADVANCED_RACK.get(), AdvancedRackRenderer::new);
          BlockEntityRenderers.register(
              ModBlockEntities.SKEWER_RECIPE.get(), SkewerRecipeBlockRenderer::new);
          BlockEntityRenderers.register(
              ModBlockEntities.SKEWER_PLATE.get(), SkewerPlateRenderer::new);
          register(ModItems.UNFINISHED_SKEWER.get());
          register(ModItems.SECRET_SKEWER.get());
          register(ModItems.MYSTERIOUS_SKEWER.get());
          register(ModItems.DARK_GRILLING.get());
          ModItems.RAW_SKEWERS.forEach(item -> registerFixed(item.get()));
          ModItems.FIXED_SKEWERS.forEach(item -> registerFixed(item.get()));
          net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
              .filter(ClientSetup::isGeneratedKubeSkewer)
              .forEach(ClientSetup::registerGeneratedSkewer);
          Item cookedSlimeSkewer =
              net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                  ResourceLocation.fromNamespaceAndPath(
                      KaleidoscopeGrilling.MOD_ID, "grilled_slime_skewer"));
          ItemProperties.register(
              cookedSlimeSkewer,
              SLIME_SKEWER_FRAME,
              (stack, level, entity, seed) ->
                  level == null ? 0.0F : (level.getGameTime() / 4L % 5L) / 4.0F);
          ItemProperties.register(
              ModItems.MYSTERIOUS_SKEWER.get(),
              FAILED_SKEWER_SOURCE,
              (stack, level, entity, seed) -> FailedSkewerData.modelValue(stack));
          ItemProperties.register(
              ModItems.DARK_GRILLING.get(),
              FAILED_SKEWER_SOURCE,
              (stack, level, entity, seed) -> FailedSkewerData.modelValue(stack));
          Item oilPot =
              net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                  ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "oil_pot"));
          ItemProperties.register(
              oilPot,
              OIL_TYPE,
              (stack, level, entity, seed) ->
                  OilPotCompat.getCount(stack) > 0
                      ? oilTypeModelValue(OilPotCompat.getType(stack))
                      : 0F);
          ItemProperties.register(
              oilPot,
              OIL_BRUSHING,
              (stack, level, entity, seed) -> oilBrushModelValue(stack, entity));
          Block oilPotBlock =
              net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                  ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "oil_pot"));
          ItemBlockRenderTypes.setRenderLayer(oilPotBlock, RenderType.cutout());
          ItemBlockRenderTypes.setRenderLayer(
              ModBlocks.SEASONING_BOTTLE.get(), RenderType.translucent());
          ItemBlockRenderTypes.setRenderLayer(ModBlocks.BIG_VAT.get(), RenderType.translucent());
          ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_RACK.get(), RenderType.cutout());
          ItemBlockRenderTypes.setRenderLayer(ModBlocks.SKEWER_RECIPE.get(), RenderType.cutout());
          ItemBlockRenderTypes.setRenderLayer(ModBlocks.SKEWER_PLATE.get(), RenderType.cutout());
          ItemBlockRenderTypes.setRenderLayer(ModBlocks.PEPPER_LEAVES.get(), RenderType.cutout());
          ItemBlockRenderTypes.setRenderLayer(ModBlocks.PEPPER_SAPLING.get(), RenderType.cutout());
          ItemProperties.register(
              ModItems.EMPTY_SEASONING_BOTTLE.get(),
              SEASONING_FILL,
              (stack, level, entity, seed) -> Math.min(8, SeasoningData.get(stack).size()) / 8.0F);
          ItemProperties.register(
              ModItems.PENDING_SEASONING.get(),
              SEASONING_FILL,
              (stack, level, entity, seed) -> Math.min(8, SeasoningData.get(stack).size()) / 8.0F);
          ItemProperties.register(
              ModItems.SPECIAL_SEASONING.get(),
              SEASONING_REMAINING,
              (stack, level, entity, seed) ->
                  Math.min(
                          8,
                          (SeasoningData.MAX_USES - SeasoningData.getUses(stack) + 1) / 2)
                      / 8.0F);
          ItemProperties.register(
              ModItems.SPECIAL_SEASONING.get(),
              SEASONING_VARIANT,
              (stack, level, entity, seed) -> SeasoningData.getVariant(stack) / 7.0F);
        });
  }

  @SubscribeEvent
  public static void registerScreens(RegisterMenuScreensEvent event) {
    event.register(ModMenus.ADVANCED_RACK.get(), AdvancedRackScreen::new);
    event.register(ModMenus.RACK_SELECTION.get(), RackSelectionScreen::new);
    event.register(ModMenus.RACK_SHORTCUT.get(), RackShortcutScreen::new);
  }

  @SubscribeEvent
  public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    event.register(
        SkewerColorProvider::color, ModItems.UNFINISHED_SKEWER.get(), ModItems.SECRET_SKEWER.get());
    ModItems.RAW_SKEWERS.forEach(item -> event.register(SkewerColorProvider::color, item.get()));
    ModItems.FIXED_SKEWERS.forEach(item -> event.register(SkewerColorProvider::color, item.get()));
    net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
        .filter(ClientSetup::isGeneratedKubeSkewer)
        .forEach(item -> event.register(SkewerColorProvider::color, item));
    event.register(
        SeasoningColorProvider::color,
        ModItems.EMPTY_SEASONING_BOTTLE.get(),
        ModItems.PENDING_SEASONING.get());
  }

  @SubscribeEvent
  public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
    SkewerGuiDecorator decorator = new SkewerGuiDecorator();
    event.register(ModItems.UNFINISHED_SKEWER.get(), decorator);
    event.register(ModItems.SECRET_SKEWER.get(), decorator);
    net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
        .filter(ClientSetup::isGeneratedKubeSkewer)
        .forEach(item -> event.register(item, decorator));
  }

  /** Called after a server-script sync, including after /reload. */
  public static void refreshScriptSkewerRendering() {
    Minecraft minecraft = Minecraft.getInstance();
    net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
        .filter(
            item ->
                SkewerRecipes.usesGeneratedModel(
                    new net.minecraft.world.item.ItemStack(item)))
        .forEach(
            item -> {
              registerGeneratedSkewer(item);
              minecraft.getItemColors().register(SkewerColorProvider::color, item);
            });
    SkewerColorProvider.clearCache();
    SkewerGuiIconCache.clear();
  }

  @SubscribeEvent
  public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
    event.register(
        ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "item/skewer_plate_base")));
    for (String base : new String[] {"fish_skewer", "caterpillar_skewer"}) {
      event.register(
          ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                  KaleidoscopeGrilling.MOD_ID,
                  fixedPiecePath(base, "_raw_piece_1"))));
      event.register(
          ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                  KaleidoscopeGrilling.MOD_ID,
                  fixedPiecePath(base, "_piece_1"))));
    }
    for (String base :
        new String[] {
          "ender_pearl_skewer",
          "golden_skewer",
          "lamb_skewer",
          "meat_and_bone_skewer",
          "meatball_skewer",
          "mid_wing_skewer",
          "mushroom_skewer",
          "ordinary_skewer",
          "potato_slice_skewer",
          "squid_tentacle_skewer"
        }) {
      event.register(
          ModelResourceLocation.standalone(
              ResourceLocation.fromNamespaceAndPath(
                  KaleidoscopeGrilling.MOD_ID,
                  fixedPiecePath(base, "_piece_3"))));
      if (!base.equals("ordinary_skewer")) {
        event.register(
            ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(
                    KaleidoscopeGrilling.MOD_ID,
                    fixedPiecePath(base, "_raw_piece_3"))));
      }
    }
    event.register(
        ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "block/pot_oil_default")));
    event.register(
        ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "block/pot_oil_canola")));
    event.register(
        ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "block/pot_oil_secret_chili")));
    event.register(
        ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(
                KaleidoscopeGrilling.MOD_ID, "block/pot_oil_premium_chili")));
  }

  @SubscribeEvent
  public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
    event.registerFluidType(
        fluid("block/water_still", "block/water_flow", 0xFFC08A24), ModFluids.CANOLA_TYPE.get());
    event.registerFluidType(
        fluid("block/water_still", "block/water_flow", 0xFFE04B2A), ModFluids.SECRET_TYPE.get());
    event.registerFluidType(
        fluid("block/lava_still", "block/lava_flow", 0xFF9E1B16), ModFluids.PREMIUM_TYPE.get());
  }

  @SubscribeEvent
  public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(
        (ResourceManagerReloadListener) manager -> SkewerGuiIconCache.clear());
    event.registerReloadListener(
        (ResourceManagerReloadListener) manager -> SkewerColorProvider.clearCache());
    event.registerReloadListener(
        (ResourceManagerReloadListener) manager -> SeasoningColorProvider.clearCache());
  }

  private static void register(Item item) {
    ItemProperties.register(
        item,
        SKEWER_STATE,
        (stack, level, entity, seed) -> SkeweringHandler.modelState(stack) / 64.0F);
    ItemProperties.register(
        item,
        SKEWER_OUTLINE,
        (stack, level, entity, seed) -> SkewerOutlineRender.isActive() ? 1.0F : 0.0F);
    if (item instanceof MultiBiteSkewerItem)
      ItemProperties.register(
          item,
          SKEWER_BITE_STAGE,
          (stack, level, entity, seed) -> MultiBiteSkewerItem.visualBiteStage(stack, entity));
  }

  private static void registerFixed(Item item) {
    ItemProperties.register(
        item,
        SKEWER_HOT,
        (stack, level, entity, seed) ->
            level != null && FoodState.isHot(stack, level) ? 1.0F : 0.0F);
    ItemProperties.register(
        item,
        SKEWER_COOKING_STAGE,
        (stack, level, entity, seed) -> SecretSkewerItem.getVisualStage(stack) / 5.0F);
    if (item instanceof MultiBiteSkewerItem)
      ItemProperties.register(
          item,
          SKEWER_BITE_STAGE,
          (stack, level, entity, seed) -> MultiBiteSkewerItem.visualBiteStage(stack, entity));
  }

  private static void registerGeneratedSkewer(Item item) {
    register(item);
    registerFixed(item);
  }

  private static boolean isGeneratedKubeSkewer(Item item) {
    if (!item.getClass().getName().equals(
        "cn.breezeth.kaleidoscope_grilling.kubejs.KubeSkewerItem")) return false;
    try {
      return Boolean.TRUE.equals(item.getClass().getMethod("usesGeneratedModel").invoke(item));
    } catch (ReflectiveOperationException ignored) {
      return false;
    }
  }

  private static float oilTypeModelValue(String type) {
    return switch (type) {
      case "canola" -> 0.25F;
      case "secret_chili" -> 0.5F;
      case "premium_chili" -> 0.75F;
      default -> 0F;
    };
  }

  private static String fixedPiecePath(String base, String suffix) {
    String folder = base.endsWith("_skewer") ? base.substring(0, base.length() - 7) : base;
    return "item/fixed_skewers/" + folder + "/" + base + suffix;
  }

  private static float oilBrushModelValue(
      net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
    if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return 0F;
    AnvilPressAnimationAccess animation = (AnvilPressAnimationAccess) player;
    if (animation.grilling$getOilBrushProgress(
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true))
            < 0.0F
        || player.getItemInHand(animation.grilling$getOilBrushHand()) != stack) return 0F;
    return (animation.grilling$getOilBrushType() + 1.0F) * 0.25F;
  }

  private static IClientFluidTypeExtensions fluid(String stillPath, String flowingPath, int tint) {
    return new IClientFluidTypeExtensions() {
      private final ResourceLocation still = ResourceLocation.withDefaultNamespace(stillPath);
      private final ResourceLocation flowing = ResourceLocation.withDefaultNamespace(flowingPath);

      @Override
      public ResourceLocation getStillTexture() {
        return still;
      }

      @Override
      public ResourceLocation getFlowingTexture() {
        return flowing;
      }

      @Override
      public int getTintColor() {
        return tint;
      }
    };
  }

  private ClientSetup() {}
}
