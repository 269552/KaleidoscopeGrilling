package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.AdvancedRackMenu;
import cn.breezeth.kaleidoscope_grilling.FoodState;
import cn.breezeth.kaleidoscope_grilling.HotFoodMerge;
import cn.breezeth.kaleidoscope_grilling.RackKeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.glfw.GLFW;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenHotMergeMixin {
  @Shadow protected AbstractContainerMenu menu;
  @Shadow private Slot hoveredSlot;
  @Shadow protected int leftPos;
  @Shadow protected int topPos;

  @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
  private void grilling$mergeHotFood(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    Minecraft minecraft = Minecraft.getInstance();
    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT
        || !RackKeyHandler.isCapsHeld()
        || hoveredSlot == null
        || !hoveredSlot.hasItem()
        || menu.getCarried().isEmpty()
        || menu instanceof AdvancedRackMenu
        || minecraft.gameMode == null) return;
    minecraft.gameMode.handleInventoryButtonClick(
        menu.containerId, HotFoodMerge.menuButtonForSlot(hoveredSlot.index));
    cir.setReturnValue(true);
  }

  @Inject(method = "renderSlot", at = @At("TAIL"))
  private void grilling$drawHotMergeHint(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null
        || !RackKeyHandler.isCapsHeld()
        || menu instanceof AdvancedRackMenu
        || menu.getCarried().isEmpty()
        || slot.getItem().isEmpty()
        || slot.getItem().getCount() >= slot.getItem().getMaxStackSize()
        || !FoodState.canMergeHot(slot.getItem(), menu.getCarried(), minecraft.level)) return;
    drawMergeGlyph(graphics, leftPos + slot.x + 8, topPos + slot.y);
  }

  private static void drawMergeGlyph(GuiGraphics graphics, int x, int y) {
    int shadow = 0xC018120C;
    int light = 0xE0FFD66B;
    fillGlyph(graphics, x + 1, y + 1, shadow);
    fillGlyph(graphics, x, y, light);
  }

  private static void fillGlyph(GuiGraphics graphics, int x, int y, int color) {
    graphics.fill(x, y, x + 2, y + 2, color);
    graphics.fill(x + 6, y, x + 8, y + 2, color);
    graphics.fill(x + 1, y + 2, x + 3, y + 4, color);
    graphics.fill(x + 5, y + 2, x + 7, y + 4, color);
    graphics.fill(x + 2, y + 4, x + 6, y + 6, color);
    graphics.fill(x + 3, y + 6, x + 5, y + 8, color);
  }
}
