package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HotFoodGuiBadge {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation(
          KaleidoscopeGrilling.MOD_ID, "textures/gui/hot_food_badge.png");
  private static final float DEPTH = 200.0F;

  public static void render(
      GuiGraphics graphics, ItemStack stack, Level level, int x, int y) {
    if (level == null || !FoodState.isHot(stack, level)) return;
    graphics.pose().pushPose();
    graphics.pose().translate(0.0F, 0.0F, DEPTH);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    graphics.blit(TEXTURE, x, y, 16, 16, 0.0F, 0.0F, 16, 16, 16, 16);
    RenderSystem.disableBlend();
    graphics.pose().popPose();
  }

  private HotFoodGuiBadge() {}
}
