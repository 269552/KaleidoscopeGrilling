package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.compat.CreateCompat;
import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class SeasoningJeiCategory implements IRecipeCategory<GrillingJeiRecipes.Seasoning> {
  static final RecipeType<GrillingJeiRecipes.Seasoning> TYPE =
      RecipeType.create(
          KaleidoscopeGrilling.MOD_ID, "seasoning", GrillingJeiRecipes.Seasoning.class);
  private static final ResourceLocation BACKGROUND =
      new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "textures/gui/jei/seasoning.png");
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
    return CreateCompat.loaded() ? 110 : 74;
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

  @Override
  public void draw(
      GrillingJeiRecipes.Seasoning recipe,
      IRecipeSlotsView recipeSlotsView,
      GuiGraphics graphics,
      double mouseX,
      double mouseY) {
    Component label = Component.translatable("jei.kaleidoscope_grilling.seasoning.optional");
    var font = Minecraft.getInstance().font;
    graphics.drawString(font, label, 46 - font.width(label) / 2, 64, 0xFF777777, false);
    if (CreateCompat.loaded()) {
      Component automation =
          Component.translatable("jei.kaleidoscope_grilling.seasoning.create_automation");
      graphics.drawString(
          font, automation, 110 - font.width(automation) / 2, 77, 0xFF777777, false);
      Component filterHint =
          Component.translatable("jei.kaleidoscope_grilling.seasoning.create_filter");
      graphics.drawString(
          font, filterHint, 110 - font.width(filterHint) / 2, 88, 0xFF777777, false);
      Component deployerHint =
          Component.translatable("jei.kaleidoscope_grilling.seasoning.create_deployer");
      graphics.drawString(
          font, deployerHint, 110 - font.width(deployerHint) / 2, 99, 0xFF777777, false);
    }
  }

  private static List<ItemStack> rotated(List<ItemStack> choices, int offset) {
    if (choices.isEmpty()) return choices;
    List<ItemStack> choicesWithBlank = new java.util.ArrayList<>(choices);
    choicesWithBlank.add(null);
    return java.util.stream.IntStream.range(0, choicesWithBlank.size())
        .mapToObj(index -> choicesWithBlank.get((index + offset) % choicesWithBlank.size()))
        .toList();
  }
}
