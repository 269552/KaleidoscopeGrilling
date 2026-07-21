package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.IItemDecorator;

public final class SkewerGuiDecorator implements IItemDecorator {
  private static final int RAW_COLOR = 0xFFFFFFFF;
  private static final int COOKED_COLOR = 0xFFFFFF33;
  private static final int HOT_COLOR = 0xFFFF3B30;

  @Override
  public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
    // The cached icon already contains its outline. Keep the direct renderer as the fallback.
    if (SkewerGuiIconCache.hasCachedIcon(stack)) return false;
    SkewerOutlineRender.renderItem(graphics, stack, x, y, colorFor(stack));
    return false;
  }

  public static int colorFor(ItemStack stack) {
    int color = RAW_COLOR;
    if ((stack.is(ModItems.SECRET_SKEWER.get()) && SecretSkewerItem.isCooked(stack))
        || SkewerRecipes.isCookedSkewer(stack)) {
      Level level = Minecraft.getInstance().level;
      color = level != null && FoodState.isHot(stack, level) ? HOT_COLOR : COOKED_COLOR;
    }
    return color;
  }
}
