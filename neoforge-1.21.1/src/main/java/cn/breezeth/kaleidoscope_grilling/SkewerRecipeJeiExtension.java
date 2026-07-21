package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

final class SkewerRecipeJeiExtension
    implements ICraftingCategoryExtension<SkewerRecipeCraftingRecipe> {
  @Override
  public void setRecipe(
      RecipeHolder<SkewerRecipeCraftingRecipe> recipe,
      IRecipeLayoutBuilder builder,
      ICraftingGridHelper craftingGridHelper,
      IFocusGroup focuses) {
    List<ItemStack> skewers = GrillingJeiRecipes.recordableSkewers();
    craftingGridHelper.createAndSetInputs(
        builder,
        List.of(skewers, List.of(GrillingJeiRecipes.blankCookeryRecipe())),
        0,
        0);
    craftingGridHelper.createAndSetOutputs(builder, GrillingJeiRecipes.recordedSkewerBooks());
  }
}
