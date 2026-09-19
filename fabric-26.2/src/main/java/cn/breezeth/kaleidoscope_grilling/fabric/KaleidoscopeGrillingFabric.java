package cn.breezeth.kaleidoscope_grilling.fabric;

import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;
import net.fabricmc.api.ModInitializer;

/** Fabric entrypoint for the faithful Minecraft 26.2 port. */
public final class KaleidoscopeGrillingFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    // Registries are being moved one-for-one from the original NeoForge source.
    // Sounds are the first completed registry; blocks/items/block entities follow.
    ModSounds.init();
  }
}
