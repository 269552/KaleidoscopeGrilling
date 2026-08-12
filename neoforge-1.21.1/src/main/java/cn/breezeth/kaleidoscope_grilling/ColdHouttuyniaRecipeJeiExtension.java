package cn.breezeth.kaleidoscope_grilling;


import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

final class ColdHouttuyniaRecipeJeiExtension
    implements ICraftingCategoryExtension<ColdHouttuyniaCraftingRecipe> {
  @Override
  public void setRecipe(
      RecipeHolder<ColdHouttuyniaCraftingRecipe> recipe,
      IRecipeLayoutBuilder builder,
      ICraftingGridHelper craftingGridHelper,
      IFocusGroup focuses) {
    ItemStack houttuynia = new ItemStack(ModItems.HOUTTUYNIA.get());
    ItemStack oilPot = GrillingJeiRecipes.premiumChiliOilPot(2);
    craftingGridHelper.createAndSetInputs(
        builder,
        List.of(
            List.of(houttuynia),
            List.of(houttuynia),
            List.of(houttuynia),
            List.of(oilPot)),
        0,
        0);
    craftingGridHelper.createAndSetOutputs(
        builder, List.of(new ItemStack(ModItems.COLD_HOUTTUYNIA.get())));
  }
}
