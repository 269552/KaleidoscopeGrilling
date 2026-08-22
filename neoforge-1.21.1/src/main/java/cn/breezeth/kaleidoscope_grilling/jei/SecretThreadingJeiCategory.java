package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
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
    icon = SkewerJeiRenderer.drawable(GrillingJeiRecipes.secretThreadingExample());
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
    return 134;
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder,
      GrillingJeiRecipes.Threading recipe,
      IFocusGroup focuses) {
    List<List<ItemStack>> ingredients = recipe.ingredients();
    ItemStack focused =
        focuses
            .getItemStackFocuses(RecipeIngredientRole.INPUT)
            .map(focus -> focus.getTypedValue().getIngredient())
            .filter(
                stack ->
                    ingredients.stream()
                        .flatMap(List::stream)
                        .anyMatch(choice -> choice.getItem() == stack.getItem()))
            .findFirst()
            .orElse(ItemStack.EMPTY);
    for (int i = 0; i < ingredients.size() && i < FOOD_SLOTS.length; i++) {
      List<ItemStack> choices = ingredients.get(i);
      if (!focused.isEmpty()) {
        choices =
            i == 0
                ? List.of(focused)
                : choices.stream().filter(stack -> stack.getItem() != focused.getItem()).toList();
      }
      builder
          .addInputSlot(FOOD_SLOTS[i][0], FOOD_SLOTS[i][1])
          .setSlotName("food_" + (i + 1))
          .addItemStacks(rotated(choices, i));
    }
    builder.addInputSlot(8, 87).setSlotName("stick").addItemStack(new ItemStack(Items.STICK));
    builder
        .addOutputSlot(123, 57)
        .setSlotName("result")
        .setCustomRenderer(VanillaTypes.ITEM_STACK, SkewerJeiRenderer.INSTANCE)
        .addItemStack(recipe.result());
  }

  @Override
  public void draw(
      GrillingJeiRecipes.Threading recipe,
      IRecipeSlotsView recipeSlotsView,
      GuiGraphics graphics,
      double mouseX,
      double mouseY) {
    ThreadingJeiCategory.drawThreadingHint(graphics);
  }

  private static List<ItemStack> rotated(List<ItemStack> choices, int offset) {
    if (choices.isEmpty()) return choices;
    return java.util.stream.IntStream.range(0, choices.size())
        .mapToObj(index -> choices.get((index + offset) % choices.size()))
        .toList();
  }
}
