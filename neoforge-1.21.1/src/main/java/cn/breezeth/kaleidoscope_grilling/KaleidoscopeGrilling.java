package cn.breezeth.kaleidoscope_grilling;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KaleidoscopeGrilling.MOD_ID)
public final class KaleidoscopeGrilling {
  public static final String MOD_ID = "kaleidoscope_grilling";

  public KaleidoscopeGrilling(IEventBus modBus, ModContainer modContainer) {
    modContainer.registerConfig(ModConfig.Type.COMMON, HotFoodConfig.SPEC);
    ModItems.ITEMS.register(modBus);
    ModEffects.EFFECTS.register(modBus);
    ModSounds.SOUNDS.register(modBus);
    ModBlocks.BLOCKS.register(modBus);
    ModBlockEntities.BLOCK_ENTITIES.register(modBus);
    ModMenus.MENUS.register(modBus);
    ModRecipeSerializers.SERIALIZERS.register(modBus);
    ModFeatures.FEATURES.register(modBus);
    ModFluids.FLUID_TYPES.register(modBus);
    ModFluids.FLUIDS.register(modBus);
    ModCreativeTabs.TABS.register(modBus);
    modBus.addListener(CommonSetup::onSetup);
    modBus.addListener(BigVatCapabilities::register);
    NeoForge.EVENT_BUS.addListener(SkeweringHandler::onRightClickItem);
    NeoForge.EVENT_BUS.addListener(SkewerPlatePlacement::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onDeath);
    NeoForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onEntityTick);
    NeoForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickItem);
    NeoForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(SeasoningHandler::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(CropDropHandler::onBlockBreak);
    NeoForge.EVENT_BUS.addListener(KnifeDropHandler::onLivingDrops);
    NeoForge.EVENT_BUS.addListener(StrippingHandler::onBlockToolModification);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onTooltip);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onFinish);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onStart);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onStop);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onSmelted);
    NeoForge.EVENT_BUS.addListener(DragonEggPowderHandler::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(RackCommand::register);
    NeoForge.EVENT_BUS.addListener(FortressHouttuyniaHandler::onLoad);
    NeoForge.EVENT_BUS.addListener(VillagePepperLootHandler::onLoad);
    NeoForge.EVENT_BUS.addListener(FortressWartReplacementHandler::onChunkLoad);
    NeoForge.EVENT_BUS.addListener(RecipeDisplayHandler::attack);
    NeoForge.EVENT_BUS.addListener(RecipeDisplayHandler::interact);
    NeoForge.EVENT_BUS.addListener(GrillingDataManager::register);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onPlayerTick);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onBlockPlaced);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onFoodFinished);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onPlayerClone);
  }
}
