package cn.breezeth.kaleidoscope_grilling;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

@Mod(KaleidoscopeGrilling.MOD_ID)
public final class KaleidoscopeGrilling {
    public static final String MOD_ID = "kaleidoscope_grilling";

    public KaleidoscopeGrilling() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModFluids.FLUID_TYPES.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        modBus.addListener(CommonSetup::onSetup);
        MinecraftForge.EVENT_BUS.addListener(SkeweringHandler::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(SkewerRecipeBookHandler::onItemCrafted);
        MinecraftForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onDeath);
        MinecraftForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(CropDropHandler::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(KnifeDropHandler::onLivingDrops);
        MinecraftForge.EVENT_BUS.addListener(StrippingHandler::onBlockToolModification);
    }
}
