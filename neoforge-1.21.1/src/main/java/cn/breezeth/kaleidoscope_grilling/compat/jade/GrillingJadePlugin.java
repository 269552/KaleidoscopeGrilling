package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.GrillBlock;
import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.OilPressBlock;
import cn.breezeth.kaleidoscope_grilling.SeasoningBottleBlock;
import cn.breezeth.kaleidoscope_grilling.SkewerPlateBlock;
import cn.breezeth.kaleidoscope_grilling.SkewerRecipeBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import net.neoforged.fml.ModList;

@WailaPlugin
public final class GrillingJadePlugin implements IWailaPlugin {
  public static final ResourceLocation PRESS = id("oil_press");
  public static final ResourceLocation GRILL = id("grill");
  public static final ResourceLocation SEASONING = id("seasoning_bottle");
  public static final ResourceLocation OIL_POT = id("typed_oil_pot");
  public static final ResourceLocation SKEWER_RECIPE = id("skewer_recipe");
  public static final ResourceLocation SKEWER_PLATE = id("skewer_plate");
  private static final ResourceLocation COOKERY_OIL_POT =
      ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "oil_pot");

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(OilPressProvider.INSTANCE, OilPressBlock.class);
    registration.registerBlockComponent(GrillProvider.INSTANCE, GrillBlock.class);
    registration.registerBlockComponent(
        SeasoningBottleProvider.INSTANCE, SeasoningBottleBlock.class);
    registration.registerBlockComponent(TypedOilPotProvider.INSTANCE, OilPotBlock.class);
    registration.registerBlockComponent(SkewerRecipeProvider.INSTANCE, SkewerRecipeBlock.class);
    registration.registerBlockComponent(SkewerPlateProvider.INSTANCE, SkewerPlateBlock.class);
    if (ModList.get().isLoaded("touhou_little_maid"))
      cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.MaidGrillingJadeCompat.register(
          registration);
    registration.addTooltipCollectedCallback(
        (box, accessor) -> {
          if (accessor instanceof snownee.jade.api.BlockAccessor blockAccessor
              && TypedOilPotProvider.hasCustomOil(blockAccessor)) {
            box.getTooltip().remove(COOKERY_OIL_POT);
            box.getTooltip().remove(JadeIds.UNIVERSAL_ITEM_STORAGE);
          }
        });
  }

  private static ResourceLocation id(String path) {
    return ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, path);
  }
}
