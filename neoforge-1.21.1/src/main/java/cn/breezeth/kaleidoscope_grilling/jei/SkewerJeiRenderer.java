package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.skewer.SkewerGuiIconCache;
import java.util.List;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Makes JEI use the same authored 16x16 skewer icons as inventories. */
final class SkewerJeiRenderer implements IIngredientRenderer<ItemStack> {
  static final SkewerJeiRenderer INSTANCE = new SkewerJeiRenderer();

  @Override
  public void render(GuiGraphics graphics, ItemStack stack) {
    if (!SkewerGuiIconCache.render(graphics, stack, 0, 0)) graphics.renderItem(stack, 0, 0);
  }

  @Override
  public List<Component> getTooltip(ItemStack stack, TooltipFlag flag) {
    Minecraft minecraft = Minecraft.getInstance();
    Item.TooltipContext context =
        minecraft.level == null ? Item.TooltipContext.EMPTY : Item.TooltipContext.of(minecraft.level);
    return stack.getTooltipLines(context, minecraft.player, flag);
  }

  static IDrawable drawable(ItemStack stack) {
    ItemStack icon = stack.copy();
    return new IDrawable() {
      @Override public int getWidth() { return 16; }
      @Override public int getHeight() { return 16; }

      @Override
      public void draw(GuiGraphics graphics, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        INSTANCE.render(graphics, icon);
        graphics.pose().popPose();
      }
    };
  }

  private SkewerJeiRenderer() {}
}
