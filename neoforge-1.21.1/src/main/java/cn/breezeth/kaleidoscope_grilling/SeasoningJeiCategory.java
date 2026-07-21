package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class SeasoningJeiCategory implements IRecipeCategory<GrillingJeiRecipes.Seasoning> {
  static final RecipeType<GrillingJeiRecipes.Seasoning> TYPE =
      RecipeType.create(
          KaleidoscopeGrilling.MOD_ID, "seasoning", GrillingJeiRecipes.Seasoning.class);
  private static final ResourceLocation BACKGROUND =
      ResourceLocation.tryParse("kaleidoscope_grilling:textures/gui/jei/seasoning.png");
  private static final int[][] REQUIRED_SLOTS = {{21, 11}, {38, 11}, {55, 11}};
  private static final int[][] OPTIONAL_SLOTS = {{4, 45}, {21, 45}, {38, 45}, {55, 45}, {72, 45}};

  private final IDrawable background;
  private final IDrawable icon;

  SeasoningJeiCategory(IGuiHelper guiHelper) {
    background = guiHelper.createDrawable(BACKGROUND, 0, 0, 220, 74);
    icon = guiHelper.createDrawableItemLike(ModItems.EMPTY_SEASONING_BOTTLE.get());
  }

  @Override
  public RecipeType<GrillingJeiRecipes.Seasoning> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("jei.kaleidoscope_grilling.seasoning");
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
    return 220;
  }

  @Override
  public int getHeight() {
    return 74;
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder,
      GrillingJeiRecipes.Seasoning recipe,
      IFocusGroup focuses) {
    List<ItemStack> required = recipe.required();
    for (int i = 0; i < required.size() && i < REQUIRED_SLOTS.length; i++)
      builder
          .addInputSlot(REQUIRED_SLOTS[i][0], REQUIRED_SLOTS[i][1])
          .setSlotName("required_" + (i + 1))
          .addItemStack(required.get(i));
    for (int i = 0; i < recipe.optionalCount() && i < OPTIONAL_SLOTS.length; i++)
      builder
          .addInputSlot(OPTIONAL_SLOTS[i][0], OPTIONAL_SLOTS[i][1])
          .setSlotName("optional_" + (i + 1))
          .addItemStacks(rotated(recipe.optionalChoices(), i));
    builder.addOutputSlot(196, 20).setSlotName("result").addItemStack(recipe.result());
  }

  private static List<ItemStack> rotated(List<ItemStack> choices, int offset) {
    if (choices.isEmpty()) return choices;
    return java.util.stream.IntStream.range(0, choices.size())
        .mapToObj(index -> choices.get((index + offset) % choices.size()))
        .toList();
  }
}
