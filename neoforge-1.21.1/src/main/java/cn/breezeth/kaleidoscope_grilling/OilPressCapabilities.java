package cn.breezeth.kaleidoscope_grilling;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class OilPressCapabilities {
  public static void register(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.ItemHandler.BLOCK,
        ModBlockEntities.OIL_PRESS.get(),
        (press, side) -> press.itemHandler());
  }

  private OilPressCapabilities() {}
}
