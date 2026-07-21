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
import net.minecraft.world.item.Items;

final class SecretThreadingJeiCategory
    implements IRecipeCategory<GrillingJeiRecipes.Threading> {
  static final RecipeType<GrillingJeiRecipes.Threading> TYPE =
      RecipeType.create(
          KaleidoscopeGrilling.MOD_ID,
          "secret_threading",
          GrillingJeiRecipes.Threading.class);
  private static final ResourceLocation BACKGROUND =
      ResourceLocation.tryParse("kaleidoscope_grilling:textures/gui/jei/threading.png");
  private static final int[][] FOOD_SLOTS = {{46, 63}, {59, 41}, {72, 19}};

  private final IDrawable background;
  private final IDrawable icon;

  SecretThreadingJeiCategory(IGuiHelper guiHelper) {
    background = guiHelper.createDrawable(BACKGROUND, 0, 0, 147, 110);
    icon = guiHelper.createDrawableItemStack(GrillingJeiRecipes.secretThreadingExample());
  }

  @Override
  public RecipeType<GrillingJeiRecipes.Threading> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("jei.kaleidoscope_grilling.secret_threading");
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
    return 147;
  }

  @Override
  public int getHeight() {
    return 110;
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder,
      GrillingJeiRecipes.Threading recipe,
      IFocusGroup focuses) {
    List<List<ItemStack>> ingredients = recipe.ingredients();
    for (int i = 0; i < ingredients.size() && i < FOOD_SLOTS.length; i++)
      builder
          .addInputSlot(FOOD_SLOTS[i][0], FOOD_SLOTS[i][1])
          .setSlotName("food_" + (i + 1))
          .addItemStacks(ingredients.get(i));
    builder.addInputSlot(8, 87).setSlotName("stick").addItemStack(new ItemStack(Items.STICK));
    builder.addOutputSlot(123, 57).setSlotName("result").addItemStack(recipe.result());
  }
}
