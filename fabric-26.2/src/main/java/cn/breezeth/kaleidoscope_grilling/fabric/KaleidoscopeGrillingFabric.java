package cn.breezeth.kaleidoscope_grilling.fabric;

import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint for the faithful Minecraft 26.2 port.
 *
 * The implementation intentionally uses the original source tree as the porting baseline;
 * loader-specific registration/event bridges are migrated into Fabric rather than replaced
 * with preview-only stand-ins.
 */
public final class KaleidoscopeGrillingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Registration and event wiring are being ported from the original NeoForge initializer
        // subsystem-by-subsystem. Keep this entrypoint minimal until those bridges compile.
    }
}
