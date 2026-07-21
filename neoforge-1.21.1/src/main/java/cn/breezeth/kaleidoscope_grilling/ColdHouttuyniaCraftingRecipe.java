package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ColdHouttuyniaCraftingRecipe extends CustomRecipe {
  private static final TagKey<Item> HOUTTUYNIA =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(
              KaleidoscopeGrilling.MOD_ID, "ingredients/houttuynia"));

  public ColdHouttuyniaCraftingRecipe(CraftingBookCategory category) {
    super(category);
  }

  @Override
  public boolean matches(CraftingInput input, Level level) {
    return findOilPot(input) >= 0 && countHouttuynia(input) == 3 && occupiedSlots(input) == 4;
  }

  @Override
  public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
    return valid(input) ? new ItemStack(ModItems.COLD_HOUTTUYNIA.get()) : ItemStack.EMPTY;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    NonNullList<ItemStack> remaining = super.getRemainingItems(input);
    int oilSlot = findOilPot(input);
    if (oilSlot >= 0) {
      ItemStack oilPot = input.getItem(oilSlot).copyWithCount(1);
      OilPotCompat.consume(oilPot, 2);
      remaining.set(oilSlot, oilPot);
    }
    return remaining;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 4;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return ModRecipeSerializers.COLD_HOUTTUYNIA.get();
  }

  private static boolean valid(CraftingInput input) {
    return findOilPot(input) >= 0 && countHouttuynia(input) == 3 && occupiedSlots(input) == 4;
  }

  private static int findOilPot(CraftingInput input) {
    int found = -1;
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
      if (!OilPotCompat.isOilPot(stack)) continue;
      if (found >= 0
          || !"premium_chili".equals(OilPotCompat.getType(stack))
          || OilPotCompat.getCount(stack) < 2) return -1;
      found = i;
    }
    return found;
  }

  private static int countHouttuynia(CraftingInput input) {
    int count = 0;
    for (int i = 0; i < input.size(); i++) {
      if (input.getItem(i).is(HOUTTUYNIA)) count++;
    }
    return count;
  }

  private static int occupiedSlots(CraftingInput input) {
    int count = 0;
    for (int i = 0; i < input.size(); i++) {
      if (!input.getItem(i).isEmpty()) count++;
    }
    return count;
  }
}
