package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;


import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;

final class ClearSeasoningJeiExtension implements ICraftingCategoryExtension {
  private static final List<String> BASE_SEASONINGS =
      List.of(
          "kaleidoscope_grilling:green_chili_powder",
          "kaleidoscope_grilling:sichuan_pepper",
          "kaleidoscope_grilling:onion_powder");

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder,
      ICraftingGridHelper craftingGridHelper,
      IFocusGroup focuses) {
    craftingGridHelper.createAndSetInputs(builder, List.of(inputs()), 0, 0);
    craftingGridHelper.createAndSetOutputs(
        builder, List.of(new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get())));
  }

  private static List<ItemStack> inputs() {
    List<ItemStack> inputs = new ArrayList<>();
    ItemStack partial = new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get());
    SeasoningData.set(partial, List.of(BASE_SEASONINGS.get(0)));
    inputs.add(partial);
    ItemStack pending = new ItemStack(ModItems.PENDING_SEASONING.get());
    SeasoningData.set(pending, BASE_SEASONINGS);
    inputs.add(pending);
    for (int uses : new int[] {0, 8, 15}) {
      ItemStack special = new ItemStack(ModItems.SPECIAL_SEASONING.get());
      SeasoningData.set(special, BASE_SEASONINGS);
      SeasoningData.setUses(special, uses);
      inputs.add(special);
    }
    return inputs;
  }
}
