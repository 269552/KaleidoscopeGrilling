package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.BigVatBlockEntity;
import cn.breezeth.kaleidoscope_grilling.CreateCompat;
import cn.breezeth.kaleidoscope_grilling.OilPressBlockEntity;
import cn.breezeth.kaleidoscope_grilling.OilPressContainerApi;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum OilPressProvider implements IBlockComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    if (!(accessor.getBlockEntity() instanceof OilPressBlockEntity press)) return;
    String state = press.progress() > 0 || press.waitingForContainer() ? "progress" : "cakes";
    tooltip.add(
        Component.translatable(
            "jade.kaleidoscope_grilling.press." + state,
            state.equals("progress") ? press.progress() : press.cakes(),
            state.equals("progress")
                ? OilPressBlockEntity.REQUIRED_PROGRESS
                : OilPressBlockEntity.MAX_CAKES));

    OilPressContainerApi.TransferResult result =
        OilPressContainerApi.probeNearby(
            accessor.getLevel(), accessor.getPosition(), OilPressBlockEntity.MAX_CAKES);
    BigVatBlockEntity vat =
        result.containerPos() != null
                && accessor.getLevel().getBlockEntity(result.containerPos())
                    instanceof BigVatBlockEntity found
            ? found
            : null;
    Component container;
    if (result.status() == OilPressContainerApi.TransferStatus.NO_CONTAINER) {
      container = Component.translatable("jade.kaleidoscope_grilling.press.vat.none");
    } else if (result.status() == OilPressContainerApi.TransferStatus.INCOMPATIBLE) {
      container = Component.translatable("jade.kaleidoscope_grilling.press.vat.incompatible");
    } else if (vat != null) {
      String key = result.status() == OilPressContainerApi.TransferStatus.FULL ? "full" : "ready";
      container =
          Component.translatable(
              "jade.kaleidoscope_grilling.press.vat." + key,
              vat.buckets(),
              BigVatBlockEntity.CAPACITY_BUCKETS);
    } else {
      String key = result.status() == OilPressContainerApi.TransferStatus.FULL ? "full" : "ready";
      long[] tank =
          result.containerPos() != null
              ? CreateCompat.fluidTankCapacity(accessor.getLevel(), result.containerPos())
              : null;
      if (tank != null) {
        container =
            Component.translatable("jade.kaleidoscope_grilling.press.tank." + key, tank[0], tank[1]);
      } else {
        container = Component.translatable("jade.kaleidoscope_grilling.press.container." + key);
      }
    }
    tooltip.add(container);
  }

  @Override
  public ResourceLocation getUid() {
    return GrillingJadePlugin.PRESS;
  }
}
