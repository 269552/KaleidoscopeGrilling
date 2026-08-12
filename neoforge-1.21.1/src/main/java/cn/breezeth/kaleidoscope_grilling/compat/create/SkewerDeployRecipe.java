package cn.breezeth.kaleidoscope_grilling.compat.create;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/** Placeholder recipe so a Create Deployer recognises skewerable targets; skewering happens in the mixin. */
public final class SkewerDeployRecipe implements Recipe<RecipeWrapper> {
  @Override
  public boolean matches(RecipeWrapper inv, Level level) {
    return true;
  }

  @Override
  public ItemStack assemble(RecipeWrapper inv, HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean canCraftInDimensions(int w, int h) {
    return true;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return RecipeSerializer.SHAPELESS_RECIPE;
  }

  @Override
  public RecipeType<?> getType() {
    return RecipeType.CRAFTING;
  }

  @Override
  public String getGroup() {
    return "";
  }
}
