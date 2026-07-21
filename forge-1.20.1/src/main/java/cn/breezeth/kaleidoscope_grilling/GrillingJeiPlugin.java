package cn.breezeth.kaleidoscope_grilling;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

@JeiPlugin
public final class GrillingJeiPlugin implements IModPlugin {
  private static final ResourceLocation ID =
      new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "jei_plugin");

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
    registration.addRecipes(
        ThreadingJeiCategory.TYPE, GrillingJeiRecipes.fixedThreadingRecipes());
    registration.addRecipes(
        SecretThreadingJeiCategory.TYPE, GrillingJeiRecipes.secretThreadingRecipes());
    registration.addRecipes(GrillJeiCategory.TYPE, GrillingJeiRecipes.grillingRecipes());
    registration.addRecipes(SeasoningJeiCategory.TYPE, GrillingJeiRecipes.seasoningRecipes());
    registration.addRecipes(OilPressJeiCategory.TYPE, GrillingJeiRecipes.oilPressRecipes());
  }

  @Override
  public void registerVanillaCategoryExtensions(
      IVanillaCategoryExtensionRegistration registration) {
    registration
        .getCraftingCategory()
        .addCategoryExtension(
            SkewerRecipeCraftingRecipe.class, recipe -> new SkewerRecipeJeiExtension());
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
