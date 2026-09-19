package cn.breezeth.kaleidoscope_grilling.fabric;

import cn.breezeth.kaleidoscope_grilling.registry.ModEffects;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;
import net.fabricmc.api.ModInitializer;

/** Fabric entrypoint for the faithful Minecraft 26.2 port. */
public final class KaleidoscopeGrillingFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    ModSounds.init();
    ModEffects.init();
  }
}
