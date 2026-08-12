package cn.breezeth.kaleidoscope_grilling.seasoning;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModRecipeSerializers;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ClearSeasoningCraftingRecipe extends CustomRecipe {
  public ClearSeasoningCraftingRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return isSingleSeasoningBottle(input);
  }

  @Override
  public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
    return isSingleSeasoningBottle(input)
        ? new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get())
        : ItemStack.EMPTY;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 1;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ModRecipeSerializers.CLEAR_SEASONING.get();
  }

  private static boolean isSingleSeasoningBottle(CraftingInput input) {
    boolean found = false;
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (stack.isEmpty()) continue;
      if (found
          || (!stack.is(ModItems.PENDING_SEASONING.get())
              && !stack.is(ModItems.SPECIAL_SEASONING.get())
              && !(stack.is(ModItems.EMPTY_SEASONING_BOTTLE.get())
                  && !SeasoningData.get(stack).isEmpty()))) return false;
      found = true;
    }
    return found;
  }
}
