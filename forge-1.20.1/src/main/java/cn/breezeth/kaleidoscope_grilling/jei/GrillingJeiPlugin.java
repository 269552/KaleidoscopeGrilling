package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import com.mojang.logging.LogUtils;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;

import cn.breezeth.kaleidoscope_grilling.seasoning.ClearSeasoningCraftingRecipe;
import cn.breezeth.kaleidoscope_grilling.food.ColdHouttuyniaCraftingRecipe;
import cn.breezeth.kaleidoscope_grilling.rack.RackSelectionScreen;
import cn.breezeth.kaleidoscope_grilling.rack.RackShortcutScreen;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipeCraftingRecipe;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import java.util.List;
import org.slf4j.Logger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

@JeiPlugin
public final class GrillingJeiPlugin implements IModPlugin {
  private static final ResourceLocation ID =
      new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "jei_plugin");
  private static final Logger LOGGER = LogUtils.getLogger();
  private static IJeiRuntime runtime;
  private static List<GrillingJeiRecipes.Threading> registeredThreadingRecipes = List.of();
  private static List<GrillingJeiRecipes.Grilling> registeredGrillingRecipes = List.of();
  private static boolean pendingThreadingRefresh;

  @Override
  public ResourceLocation getPluginUid() {
    return ID;
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    var guiHelper = registration.getJeiHelpers().getGuiHelper();
    registration.addRecipeCategories(
        new ThreadingJeiCategory(guiHelper),
        new SecretThreadingJeiCategory(guiHelper),
        new GrillJeiCategory(guiHelper),
        new SeasoningJeiCategory(guiHelper),
        new OilPressJeiCategory(guiHelper));
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    registeredThreadingRecipes = GrillingJeiRecipes.fixedThreadingRecipes();
    LOGGER.info("Registered {} threading recipes with JEI", registeredThreadingRecipes.size());
    registration.addRecipes(
        ThreadingJeiCategory.TYPE, registeredThreadingRecipes);
    registration.addRecipes(
        SecretThreadingJeiCategory.TYPE, GrillingJeiRecipes.secretThreadingRecipes());
    registeredGrillingRecipes = GrillingJeiRecipes.grillingRecipes();
    registration.addRecipes(GrillJeiCategory.TYPE, registeredGrillingRecipes);
    registration.addRecipes(SeasoningJeiCategory.TYPE, GrillingJeiRecipes.seasoningRecipes());
    registration.addRecipes(OilPressJeiCategory.TYPE, GrillingJeiRecipes.oilPressRecipes());
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    runtime = jeiRuntime;
    if (pendingThreadingRefresh) refreshThreadingRecipes();
  }

  @Override
  public void onRuntimeUnavailable() {
    runtime = null;
    registeredThreadingRecipes = List.of();
    registeredGrillingRecipes = List.of();
  }

  public static void refreshThreadingRecipes() {
    pendingThreadingRefresh = true;
    if (runtime == null) return;
    pendingThreadingRefresh = false;
    var manager = runtime.getRecipeManager();
    if (!registeredThreadingRecipes.isEmpty())
      manager.hideRecipes(ThreadingJeiCategory.TYPE, registeredThreadingRecipes);
    registeredThreadingRecipes = GrillingJeiRecipes.fixedThreadingRecipes();
    if (!registeredThreadingRecipes.isEmpty())
      manager.addRecipes(ThreadingJeiCategory.TYPE, registeredThreadingRecipes);
    if (!registeredGrillingRecipes.isEmpty())
      manager.hideRecipes(GrillJeiCategory.TYPE, registeredGrillingRecipes);
    registeredGrillingRecipes = GrillingJeiRecipes.grillingRecipes();
    if (!registeredGrillingRecipes.isEmpty())
      manager.addRecipes(GrillJeiCategory.TYPE, registeredGrillingRecipes);
    LOGGER.info(
        "Refreshed JEI with {} threading and {} grilling recipes",
        registeredThreadingRecipes.size(),
        registeredGrillingRecipes.size());
  }

  @Override
  public void registerExtraIngredients(IExtraIngredientRegistration registration) {
    registration.addExtraItemStacks(GrillingJeiRecipes.filledOilPots());
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    var pots = GrillingJeiRecipes.filledOilPots();
    if (pots.isEmpty()) return;
    registration.registerSubtypeInterpreter(
        pots.get(0).getItem(),
        (stack, context) -> OilPotCompat.jeiSubtype(stack));
  }

  @Override
  public void registerVanillaCategoryExtensions(
      IVanillaCategoryExtensionRegistration registration) {
    registration
        .getCraftingCategory()
        .addCategoryExtension(
            SkewerRecipeCraftingRecipe.class, recipe -> new SkewerRecipeJeiExtension());
    registration
        .getCraftingCategory()
        .addCategoryExtension(
            ColdHouttuyniaCraftingRecipe.class,
            recipe -> new ColdHouttuyniaRecipeJeiExtension());
    registration
        .getCraftingCategory()
        .addCategoryExtension(
            ClearSeasoningCraftingRecipe.class, recipe -> new ClearSeasoningJeiExtension());
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(Items.STICK, ThreadingJeiCategory.TYPE);
    registration.addRecipeCatalyst(Items.STICK, SecretThreadingJeiCategory.TYPE);
    registration.addRecipeCatalyst(ModBlocks.GRILL_ITEM.get(), GrillJeiCategory.TYPE);
    registration.addRecipeCatalyst(
        ModItems.EMPTY_SEASONING_BOTTLE.get(), SeasoningJeiCategory.TYPE);
    registration.addRecipeCatalyst(ModBlocks.OIL_PRESS_ITEM.get(), OilPressJeiCategory.TYPE);
    registration.addRecipeCatalyst(ModBlocks.BIG_VAT_ITEM.get(), OilPressJeiCategory.TYPE);
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiScreenHandler(RackShortcutScreen.class, screen -> null);
    registration.addGuiScreenHandler(RackSelectionScreen.class, screen -> null);
  }
}
