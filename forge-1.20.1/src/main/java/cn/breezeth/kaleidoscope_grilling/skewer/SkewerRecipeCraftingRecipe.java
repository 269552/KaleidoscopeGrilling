package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModRecipeSerializers;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class SkewerRecipeCraftingRecipe extends CustomRecipe {
  private static final TagKey<net.minecraft.world.item.Item> RAW_SKEWERS =
      TagKey.create(
          Registries.ITEM, new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "raw_skewers"));

  public SkewerRecipeCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
    super(id, category);
  }

  @Override
  public boolean matches(CraftingContainer container, Level level) {
    return findSkewer(container) != null && hasExactlyOneBlankRecipe(container);
  }

  @Override
  public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
    ItemStack skewer = findSkewer(container);
    if (skewer == null || !hasExactlyOneBlankRecipe(container)) return ItemStack.EMPTY;
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
  public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
    NonNullList<ItemStack> remaining = super.getRemainingItems(container);
    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
      if (isRecordableSkewer(stack)) remaining.set(i, stack.copyWithCount(1));
    }
    return remaining;
  }

  private static ItemStack findSkewer(CraftingContainer container) {
    ItemStack found = null;
    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
      if (stack.isEmpty() || stack.getItem() instanceof RecipeItem) continue;
      if (!isRecordableSkewer(stack) || found != null) return null;
      found = stack;
    }
    return found;
  }

  private static boolean isRecordableSkewer(ItemStack stack) {
    return stack.is(RAW_SKEWERS)
        || stack.is(ModItems.ORDINARY_SKEWER.get())
        || stack.is(ModItems.SECRET_SKEWER.get())
            && SkeweringHandler.readIngredientStacks(stack).size() == 3;
  }

  private static boolean hasExactlyOneBlankRecipe(CraftingContainer container) {
    int blankRecipes = 0;
    int occupied = 0;
    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
      if (stack.isEmpty()) continue;
      occupied++;
      if (stack.getItem() instanceof RecipeItem && !RecipeItem.hasRecipe(stack)) blankRecipes++;
    }
    return occupied == 2 && blankRecipes == 1;
  }
}
