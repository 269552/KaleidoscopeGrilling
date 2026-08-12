package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModRecipeSerializers;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class SkewerRecipeCraftingRecipe extends CustomRecipe {
  private static final TagKey<net.minecraft.world.item.Item> RAW_SKEWERS =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "raw_skewers"));

  public SkewerRecipeCraftingRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return findSkewer(input, level.registryAccess()) != null && hasExactlyOneBlankRecipe(input);
  }

  @Override
  public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
    ItemStack skewer = findSkewer(input, registries);
    if (skewer == null || !hasExactlyOneBlankRecipe(input)) return ItemStack.EMPTY;
    ItemStack result = new ItemStack(ModItems.SKEWER_RECIPE_BOOK.get());
    SkewerRecipeBookItem.setRecipeStack(result, skewer);
    return result;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ModRecipeSerializers.SKEWER_RECIPE_BOOK.get();
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    NonNullList<ItemStack> remaining = super.getRemainingItems(input);
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (isRecordableSkewer(stack)) remaining.set(i, stack.copyWithCount(1));
    }
    return remaining;
  }

  private static ItemStack findSkewer(CraftingInput input, HolderLookup.Provider registries) {
    ItemStack found = null;
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (stack.isEmpty() || stack.getItem() instanceof RecipeItem) continue;
      if (!isRecordableSkewer(stack, registries) || found != null) return null;
      found = stack;
    }
    return found;
  }

  private static boolean isRecordableSkewer(ItemStack stack, HolderLookup.Provider registries) {
    return stack.is(RAW_SKEWERS)
        || stack.is(ModItems.ORDINARY_SKEWER.get())
        || stack.is(ModItems.SECRET_SKEWER.get())
            && SkeweringHandler.readIngredientStacks(stack, registries).size() == 3;
  }

  private static boolean isRecordableSkewer(ItemStack stack) {
    return stack.is(RAW_SKEWERS)
        || stack.is(ModItems.ORDINARY_SKEWER.get())
        || stack.is(ModItems.SECRET_SKEWER.get());
  }

  private static boolean hasExactlyOneBlankRecipe(CraftingInput input) {
    int blankRecipes = 0;
    int occupied = 0;
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (stack.isEmpty()) continue;
      occupied++;
      if (stack.getItem() instanceof RecipeItem && !RecipeItem.hasRecipe(stack)) blankRecipes++;
    }
    return occupied == 2 && blankRecipes == 1;
  }
}
