package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class GrillJeiCategory implements IRecipeCategory<GrillingJeiRecipes.Grilling> {
  static final RecipeType<GrillingJeiRecipes.Grilling> TYPE =
      RecipeType.create(
          KaleidoscopeGrilling.MOD_ID, "grilling", GrillingJeiRecipes.Grilling.class);
  private static final ResourceLocation BACKGROUND =
      ResourceLocation.tryParse("kaleidoscope_grilling:textures/gui/jei/grilling.png");
  private static final int[][] INPUT_SLOTS = {{22, 29}, {39, 29}, {56, 29}};
  private static final int[][] OUTPUT_SLOTS = {{189, 57}, {206, 57}, {223, 57}};

  private final IDrawable background;
  private final IDrawable icon;

  GrillJeiCategory(IGuiHelper guiHelper) {
    background = guiHelper.createDrawable(BACKGROUND, 0, 0, 246, 106);
    icon = guiHelper.createDrawableItemLike(ModBlocks.GRILL_ITEM.get());
  }

  @Override
  public RecipeType<GrillingJeiRecipes.Grilling> getRecipeType() {
    return TYPE;
  }

  @Override
  public Component getTitle() {
    return Component.translatable("jei.kaleidoscope_grilling.grilling");
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
    return 246;
  }

  @Override
  public int getHeight() {
    return 106;
  }

  @Override
  public void setRecipe(
      IRecipeLayoutBuilder builder,
      GrillingJeiRecipes.Grilling recipe,
      IFocusGroup focuses) {
    addSlots(builder, recipe.inputs(), INPUT_SLOTS, true);
    builder
        .addInputSlot(102, 36)
        .setSlotName("oil")
        .addItemStacks(GrillingJeiRecipes.oilPots());
    builder
        .addInputSlot(149, 36)
        .setSlotName("seasoning")
        .addItemStack(new ItemStack(ModItems.SPECIAL_SEASONING.get()));
    addSlots(builder, recipe.outputs(), OUTPUT_SLOTS, false);
  }

  private static void addSlots(
      IRecipeLayoutBuilder builder, List<ItemStack> stacks, int[][] positions, boolean input) {
    for (int i = 0; i < stacks.size() && i < positions.length; i++) {
      var slot =
          input
              ? builder.addInputSlot(positions[i][0], positions[i][1])
              : builder.addOutputSlot(positions[i][0], positions[i][1]);
      slot
          .setSlotName((input ? "raw_" : "cooked_") + (i + 1))
          .setCustomRenderer(VanillaTypes.ITEM_STACK, SkewerJeiRenderer.INSTANCE)
          .addItemStack(stacks.get(i));
    }
  }
}
