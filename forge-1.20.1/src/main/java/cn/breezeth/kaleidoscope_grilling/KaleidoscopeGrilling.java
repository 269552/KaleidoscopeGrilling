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
import cn.breezeth.kaleidoscope_grilling.world.CropDropHandler;
import cn.breezeth.kaleidoscope_grilling.effect.DragonEggPowderHandler;
import cn.breezeth.kaleidoscope_grilling.world.FortressHouttuyniaHandler;
import cn.breezeth.kaleidoscope_grilling.world.FortressWartReplacementHandler;
import cn.breezeth.kaleidoscope_grilling.grill.GrillCommand;
import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodExpiryHandler;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodHandler;
import cn.breezeth.kaleidoscope_grilling.food.IngredientTooltipHandler;
import cn.breezeth.kaleidoscope_grilling.effect.InvincibleHandler;
import cn.breezeth.kaleidoscope_grilling.event.WeddingCandyHandler;
import cn.breezeth.kaleidoscope_grilling.world.KnifeDropHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.oil.OilFillingHandler;
import cn.breezeth.kaleidoscope_grilling.rack.RackCommand;
import cn.breezeth.kaleidoscope_grilling.jei.RecipeDisplayHandler;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerPlatePlacement;
import cn.breezeth.kaleidoscope_grilling.world.StrippingHandler;
import cn.breezeth.kaleidoscope_grilling.world.VillagePepperLootHandler;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
    ModRecipeSerializers.SERIALIZERS.register(modBus);
    ModFeatures.FEATURES.register(modBus);
    ModFluids.FLUID_TYPES.register(modBus);
    ModFluids.FLUIDS.register(modBus);
    ModCreativeTabs.TABS.register(modBus);
    if (ModList.get().isLoaded("touhou_little_maid")) {
      cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.MaidGrillingCompat.init();
    }
    modBus.addListener(CommonSetup::onSetup);
    MinecraftForge.EVENT_BUS.addListener(
        EventPriority.HIGHEST, true, SkeweringHandler::onRightClickItem);
    MinecraftForge.EVENT_BUS.addListener(SkewerPlatePlacement::onRightClickBlock);
    MinecraftForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onDeath);
    MinecraftForge.EVENT_BUS.addListener(AdvancedSeasoningHandler::onLivingTick);
    MinecraftForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickItem);
    MinecraftForge.EVENT_BUS.addListener(OilFillingHandler::onRightClickBlock);
    MinecraftForge.EVENT_BUS.addListener(SeasoningHandler::onRightClickBlock);
    MinecraftForge.EVENT_BUS.addListener(KnifeDropHandler::onLivingDrops);
    MinecraftForge.EVENT_BUS.addListener(StrippingHandler::onBlockToolModification);
    MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onTooltip);
    MinecraftForge.EVENT_BUS.addListener(IngredientTooltipHandler::onTooltip);
    MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onFinish);
    MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onStart);
    MinecraftForge.EVENT_BUS.addListener(
        EventPriority.HIGHEST, MultiBiteSkewerItem::onUseStop);
    MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onStop);
    MinecraftForge.EVENT_BUS.addListener(
        (net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Tick event) ->
            MultiBiteSkewerItem.onUseTick(event));
    MinecraftForge.EVENT_BUS.addListener(MultiBiteSkewerItem::onPlayerLoggedOut);
    MinecraftForge.EVENT_BUS.addListener(HotFoodHandler::onSmelted);
    MinecraftForge.EVENT_BUS.addListener(HotFoodExpiryHandler::onPlayerTick);
    MinecraftForge.EVENT_BUS.addListener(DragonEggPowderHandler::onRightClickBlock);
    MinecraftForge.EVENT_BUS.addListener(RackCommand::register);
    MinecraftForge.EVENT_BUS.addListener(GrillCommand::register);
    MinecraftForge.EVENT_BUS.addListener(FortressHouttuyniaHandler::onLoad);
    MinecraftForge.EVENT_BUS.addListener(VillagePepperLootHandler::onLoad);
    MinecraftForge.EVENT_BUS.addListener(FortressWartReplacementHandler::onChunkLoad);
    MinecraftForge.EVENT_BUS.addListener(FortressWartReplacementHandler::onServerTick);
    MinecraftForge.EVENT_BUS.addListener(RecipeDisplayHandler::attack);
    MinecraftForge.EVENT_BUS.addListener(RecipeDisplayHandler::interact);
    MinecraftForge.EVENT_BUS.addListener(GrillingDataManager::register);
    MinecraftForge.EVENT_BUS.addListener(ModAdvancements::onPlayerTick);
    MinecraftForge.EVENT_BUS.addListener(ModAdvancements::onBlockPlaced);
    MinecraftForge.EVENT_BUS.addListener(ModAdvancements::onFoodFinished);
    MinecraftForge.EVENT_BUS.addListener(ModAdvancements::onPlayerClone);
    MinecraftForge.EVENT_BUS.addListener(WeddingCandyHandler::onPlayerTick);
    MinecraftForge.EVENT_BUS.addListener(WeddingCandyHandler::onPlayerClone);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, InvincibleHandler::onDamage);
    MinecraftForge.EVENT_BUS.addListener(InvincibleHandler::onPlayerTick);
    MinecraftForge.EVENT_BUS.addListener(CropDropHandler::onBlockBreak);
  }
}
