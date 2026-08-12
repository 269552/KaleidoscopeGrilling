package cn.breezeth.kaleidoscope_grilling.food;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModRecipeSerializers;

import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;


import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ColdHouttuyniaCraftingRecipe extends CustomRecipe {
  private static final TagKey<Item> HOUTTUYNIA =
      TagKey.create(
          Registries.ITEM,
          new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "ingredients/houttuynia"));

  public ColdHouttuyniaCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
    super(id, category);
  }

  @Override
  public boolean matches(CraftingContainer container, Level level) {
    return findOilPot(container) >= 0
        && countHouttuynia(container) == 3
        && occupiedSlots(container) == 4;
  }

  @Override
  public ItemStack assemble(CraftingContainer container, RegistryAccess registries) {
    return matches(container, null)
        ? new ItemStack(ModItems.COLD_HOUTTUYNIA.get())
        : ItemStack.EMPTY;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
    NonNullList<ItemStack> remaining = super.getRemainingItems(container);
    int oilSlot = findOilPot(container);
    if (oilSlot >= 0) {
      ItemStack oilPot = container.getItem(oilSlot).copyWithCount(1);
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

  private static int findOilPot(CraftingContainer container) {
    int found = -1;
    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
      if (!OilPotCompat.isOilPot(stack)) continue;
      if (found >= 0
          || !"premium_chili".equals(OilPotCompat.getType(stack))
          || OilPotCompat.getCount(stack) < 2) return -1;
      found = i;
    }
    return found;
  }

  private static int countHouttuynia(CraftingContainer container) {
    int count = 0;
    for (int i = 0; i < container.getContainerSize(); i++) {
      if (container.getItem(i).is(HOUTTUYNIA)) count++;
    }
    return count;
  }

  private static int occupiedSlots(CraftingContainer container) {
    int count = 0;
    for (int i = 0; i < container.getContainerSize(); i++) {
      if (!container.getItem(i).isEmpty()) count++;
    }
    return count;
  }
}
