package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class ThreadingJeiCategory
    implements IRecipeCategory<GrillingJeiRecipes.Threading> {
  static final RecipeType<GrillingJeiRecipes.Threading> TYPE =
      RecipeType.create(
          KaleidoscopeGrilling.MOD_ID, "threading", GrillingJeiRecipes.Threading.class);
  private static final ResourceLocation BACKGROUND =
      new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "textures/gui/jei/threading.png");
  private static final int[][] FOOD_SLOTS = {{46, 63}, {59, 41}, {72, 19}};

  private final IDrawable background;
  private final IDrawable icon;

  ThreadingJeiCategory(IGuiHelper guiHelper) {
    background = guiHelper.createDrawable(BACKGROUND, 0, 0, 147, 110);
    icon = SkewerJeiRenderer.drawable(GrillingJeiRecipes.fixedThreadingIcon());
  }

  @Override
  public RecipeType<GrillingJeiRecipes.Threading> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("jei.kaleidoscope_grilling.fixed_threading");
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
    for (int i = 0; i < ingredients.size() && i < FOOD_SLOTS.length; i++)
      builder
          .addInputSlot(FOOD_SLOTS[i][0], FOOD_SLOTS[i][1])
          .setSlotName("food_" + (i + 1))
          .addItemStacks(ingredients.get(i));
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
    drawThreadingHint(graphics);
  }

  static void drawThreadingHint(GuiGraphics graphics) {
    var font = Minecraft.getInstance().font;
    var lines =
        font.split(Component.translatable("jei.kaleidoscope_grilling.threading.hint"), 143);
    for (int i = 0; i < lines.size(); i++) {
      var line = lines.get(i);
      graphics.drawString(font, line, (147 - font.width(line)) / 2, 112 + i * 10, 0xFF777777, false);
    }
  }
}
