package cn.breezeth.kaleidoscope_grilling.fabric;

import net.fabricmc.api.ModInitializer;

/**
 * Minecraft 26.2 Fabric entrypoint.
 *
 * Preview 1 is intentionally small and stable: it registers a representative
 * set of Grilling ingredients plus the Big Vat block so there is a real,
 * loadable Fabric jar while the full NeoForge feature set is ported in stages.
 */
public final class KaleidoscopeGrillingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PreviewContent.init();
    }
}
