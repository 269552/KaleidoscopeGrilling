package cn.breezeth.kaleidoscope_grilling;

import cn.breezeth.kaleidoscope_grilling.bootstrap.CommonSetup;
import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
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

import cn.breezeth.kaleidoscope_grilling.seasoning.AdvancedSeasoningHandler;
import cn.breezeth.kaleidoscope_grilling.oil.BigVatCapabilities;
import cn.breezeth.kaleidoscope_grilling.world.CropDropHandler;
import cn.breezeth.kaleidoscope_grilling.effect.DragonEggPowderHandler;
import cn.breezeth.kaleidoscope_grilling.world.FortressHouttuyniaHandler;
import cn.breezeth.kaleidoscope_grilling.world.FortressWartReplacementHandler;
import cn.breezeth.kaleidoscope_grilling.grill.GrillCommand;
import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.network.GrillingNetwork;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodExpiryHandler;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodHandler;
import cn.breezeth.kaleidoscope_grilling.food.IngredientTooltipHandler;
import cn.breezeth.kaleidoscope_grilling.effect.InvincibleHandler;
import cn.breezeth.kaleidoscope_grilling.world.KnifeDropHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.oil.OilFillingHandler;
import cn.breezeth.kaleidoscope_grilling.oil.OilPressCapabilities;
import cn.breezeth.kaleidoscope_grilling.rack.RackCommand;
import cn.breezeth.kaleidoscope_grilling.jei.RecipeDisplayHandler;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerPlatePlacement;
import cn.breezeth.kaleidoscope_grilling.world.StrippingHandler;
import cn.breezeth.kaleidoscope_grilling.world.VillagePepperLootHandler;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModList;
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
    modBus.addListener(GrillingNetwork::register);
    if (ModList.get().isLoaded("touhou_little_maid"))
      cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.MaidGrillingCompat.init(modBus);
    modBus.addListener(CommonSetup::onSetup);
    modBus.addListener(BigVatCapabilities::register);
    modBus.addListener(OilPressCapabilities::register);
    NeoForge.EVENT_BUS.addListener(
        EventPriority.HIGHEST, true, SkeweringHandler::onRightClickItem);
    NeoForge.EVENT_BUS.addListener(SkewerPlatePlacement::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onDeath);
    NeoForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onEntityTick);
    NeoForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickItem);
    NeoForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(SeasoningHandler::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(KnifeDropHandler::onLivingDrops);
    NeoForge.EVENT_BUS.addListener(StrippingHandler::onBlockToolModification);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onTooltip);
    NeoForge.EVENT_BUS.addListener(IngredientTooltipHandler::onTooltip);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onFinish);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onStart);
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onStop);
    NeoForge.EVENT_BUS.addListener(
        (net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Tick event) ->
            MultiBiteSkewerItem.onUseTick(event));
    NeoForge.EVENT_BUS.addListener(HotFoodHandler::onSmelted);
    NeoForge.EVENT_BUS.addListener(HotFoodExpiryHandler::onPlayerTick);
    NeoForge.EVENT_BUS.addListener(DragonEggPowderHandler::onRightClickBlock);
    NeoForge.EVENT_BUS.addListener(RackCommand::register);
    NeoForge.EVENT_BUS.addListener(GrillCommand::register);
    NeoForge.EVENT_BUS.addListener(FortressHouttuyniaHandler::onLoad);
    NeoForge.EVENT_BUS.addListener(VillagePepperLootHandler::onLoad);
    NeoForge.EVENT_BUS.addListener(FortressWartReplacementHandler::onChunkLoad);
    NeoForge.EVENT_BUS.addListener(FortressWartReplacementHandler::onServerTick);
    NeoForge.EVENT_BUS.addListener(RecipeDisplayHandler::attack);
    NeoForge.EVENT_BUS.addListener(RecipeDisplayHandler::interact);
    NeoForge.EVENT_BUS.addListener(GrillingDataManager::register);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onPlayerTick);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onBlockPlaced);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onFoodFinished);
    NeoForge.EVENT_BUS.addListener(ModAdvancements::onPlayerClone);
    NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, InvincibleHandler::onDamage);
    NeoForge.EVENT_BUS.addListener(InvincibleHandler::onPlayerTick);
    NeoForge.EVENT_BUS.addListener(CropDropHandler::onBlockBreak);
  }
}
