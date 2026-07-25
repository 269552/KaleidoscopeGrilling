package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ClearSeasoningCraftingRecipe extends CustomRecipe {
  public ClearSeasoningCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
    super(id, category);
  }

  @Override
  public boolean matches(CraftingContainer container, Level level) {
    return isSingleSeasoningBottle(container);
  }

  @Override
  public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
    return isSingleSeasoningBottle(container)
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

  private static boolean isSingleSeasoningBottle(CraftingContainer container) {
    boolean found = false;
    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
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
