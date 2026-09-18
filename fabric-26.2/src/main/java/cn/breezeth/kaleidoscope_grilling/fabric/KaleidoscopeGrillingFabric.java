package cn.breezeth.kaleidoscope_grilling.fabric;

import net.fabricmc.api.ModInitializer;

/** Minecraft 26.2 Fabric entrypoint for the staged Grilling port. */
public final class KaleidoscopeGrillingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PreviewContent.init();
    }
}
