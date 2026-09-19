package cn.breezeth.kaleidoscope_grilling.oil;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class BigVatCapabilities {
  public static void register(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.FluidHandler.BLOCK,
        ModBlockEntities.BIG_VAT.get(),
        (vat, side) -> vat.fluidHandler());
  }

  private BigVatCapabilities() {}
}
