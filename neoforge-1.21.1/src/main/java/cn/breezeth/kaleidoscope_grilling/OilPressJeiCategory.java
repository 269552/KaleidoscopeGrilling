package cn.breezeth.kaleidoscope_grilling;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class OilPressJeiCategory implements IRecipeCategory<GrillingJeiRecipes.OilPress> {
  static final RecipeType<GrillingJeiRecipes.OilPress> TYPE =
      RecipeType.create(
          KaleidoscopeGrilling.MOD_ID, "oil_pressing", GrillingJeiRecipes.OilPress.class);
  private static final ResourceLocation BACKGROUND =
      ResourceLocation.tryParse("kaleidoscope_grilling:textures/gui/jei/oil_pressing.png");

  private final IDrawable background;
  private final IDrawable icon;

  OilPressJeiCategory(IGuiHelper guiHelper) {
    background = guiHelper.createDrawable(BACKGROUND, 0, 0, 208, 108);
    icon = guiHelper.createDrawableItemLike(ModBlocks.OIL_PRESS_ITEM.get());
  }

  @Override
  public RecipeType<GrillingJeiRecipes.OilPress> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("jei.kaleidoscope_grilling.oil_pressing");
  }

  @Override
  public IDrawable getBackground() {
    return background;
  }

  @Override
  public IDrawable getIcon() {
    return icon;
  }

  @Override
  public int getWidth() {
    return 208;
  }

  @Override
  public int getHeight() {
    return 108;
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder,
      GrillingJeiRecipes.OilPress recipe,
      IFocusGroup focuses) {
    builder
        .addInputSlot(30, 73)
        .setSlotName("oil_cakes")
        .addItemStack(new ItemStack(ModItems.OIL_CAKE.get(), 4));
    builder.addInputSlot(184, 41).setSlotName("container").addItemStack(recipe.container());
    builder.addOutputSlot(184, 74).setSlotName("result").addItemStack(recipe.result());
  }
}
