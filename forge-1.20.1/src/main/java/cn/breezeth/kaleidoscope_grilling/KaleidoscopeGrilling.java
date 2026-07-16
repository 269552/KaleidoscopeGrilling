package cn.breezeth.kaleidoscope_grilling;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(KaleidoscopeGrilling.MOD_ID)
public final class KaleidoscopeGrilling {
    public static final String MOD_ID = "kaleidoscope_grilling";

    public KaleidoscopeGrilling() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HotFoodConfig.SPEC);
        ModItems.ITEMS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
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
        MinecraftForge.EVENT_BUS.addListener(SeasoningHandler::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(CropDropHandler::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(KnifeDropHandler::onLivingDrops);
        MinecraftForge.EVENT_BUS.addListener(StrippingHandler::onBlockToolModification);
        MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onFinish);
        MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onStart);
        MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onStop);
        MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onSmelted);
        MinecraftForge.EVENT_BUS.addListener(DragonEggPowderHandler::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(RackCommand::register);
        MinecraftForge.EVENT_BUS.addListener(FortressHouttuyniaHandler::onLoad);
        MinecraftForge.EVENT_BUS.addListener(VillagePepperLootHandler::onLoad);
        MinecraftForge.EVENT_BUS.addListener(FortressWartReplacementHandler::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(RecipeDisplayHandler::attack);
        MinecraftForge.EVENT_BUS.addListener(RecipeDisplayHandler::interact);
        MinecraftForge.EVENT_BUS.addListener(GrillingDataManager::register);
    }
}
