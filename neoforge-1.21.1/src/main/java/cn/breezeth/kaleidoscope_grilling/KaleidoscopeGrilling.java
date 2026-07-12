package cn.breezeth.kaleidoscope_grilling;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KaleidoscopeGrilling.MOD_ID)
public final class KaleidoscopeGrilling {
    public static final String MOD_ID = "kaleidoscope_grilling";

    public KaleidoscopeGrilling(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModFluids.FLUID_TYPES.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(CommonSetup::onSetup);
        NeoForge.EVENT_BUS.addListener(SkeweringHandler::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(SkewerRecipeBookHandler::onItemCrafted);
        NeoForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onDeath);
        NeoForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onEntityTick);
        NeoForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(CropDropHandler::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(KnifeDropHandler::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(StrippingHandler::onBlockToolModification);
    }
}
