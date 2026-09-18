package cn.breezeth.kaleidoscope_grilling.fabric;

import cn.breezeth.kaleidoscope_grilling.bootstrap.CommonSetup;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModCreativeTabs;
import cn.breezeth.kaleidoscope_grilling.registry.ModEffects;
import cn.breezeth.kaleidoscope_grilling.registry.ModFeatures;
import cn.breezeth.kaleidoscope_grilling.registry.ModFluids;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModMenus;
import cn.breezeth.kaleidoscope_grilling.registry.ModRecipeSerializers;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;
import net.fabricmc.api.ModInitializer;

/** Common Fabric entrypoint for the Minecraft 26.2 port. */
public final class KaleidoscopeGrillingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // The porting DeferredRegister adapter performs registration while each
        // registry class initializes. These calls intentionally force that order.
        ModSounds.SOUNDS.register(null);
        ModEffects.EFFECTS.register(null);
        ModBlocks.BLOCKS.register(null);
        ModItems.ITEMS.register(null);
        ModBlockEntities.BLOCK_ENTITIES.register(null);
        ModMenus.MENUS.register(null);
        ModRecipeSerializers.SERIALIZERS.register(null);
        ModFeatures.FEATURES.register(null);
        ModFluids.FLUIDS.register(null);
        ModCreativeTabs.TABS.register(null);

        CommonSetup.onSetup();
    }
}
