package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.TypedOilPotAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum TypedOilPotProvider implements IBlockComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    if (!(accessor.getBlockEntity() instanceof TypedOilPotAccess oil)) return;
    String type = oil.grilling$getOilType();
    if (!isCustomType(type)) return;
    tooltip.add(
        Component.translatable(
            "jade.kaleidoscope_grilling.oil_pot." + type, oil.grilling$getOilCount()),
        GrillingJadePlugin.OIL_POT);
  }

  static boolean hasCustomOil(BlockAccessor accessor) {
    return accessor.getBlockEntity() instanceof TypedOilPotAccess oil
        && isCustomType(oil.grilling$getOilType());
  }

  private static boolean isCustomType(String type) {
    return type.equals("canola") || type.equals("secret_chili") || type.equals("premium_chili");
  }

  @Override
  public ResourceLocation getUid() {
    return GrillingJadePlugin.OIL_POT;
  }
}
