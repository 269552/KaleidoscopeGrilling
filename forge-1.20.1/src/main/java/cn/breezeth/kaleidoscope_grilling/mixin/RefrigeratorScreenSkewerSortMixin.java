package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.compat.ordertocook.RefrigeratorSkewerSorter;
import cn.breezeth.ordertocook.screen.RefrigeratorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

@Mixin(RefrigeratorScreen.class)
abstract class RefrigeratorScreenSkewerSortMixin extends AbstractContainerScreen<AbstractContainerMenu> {
  protected RefrigeratorScreenSkewerSortMixin(
      AbstractContainerMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  @ModifyArg(
      method = "lambda$init$0",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryButtonClick(II)V"),
      index = 1,
      require = 0)
  private int grilling$selectSortMode(int originalId) {
    return Screen.hasShiftDown() ? RefrigeratorSkewerSorter.FULL_SORT_ID : originalId;
  }

  @Redirect(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V"),
      require = 0)
  private void grilling$renderSortTooltip(
      GuiGraphics graphics, Font font, Component original, int mouseX, int mouseY) {
    var level = Minecraft.getInstance().level;
    if (level == null || !RefrigeratorSkewerSorter.hasFullSortCandidate(menu.slots, level)) {
      graphics.renderTooltip(font, original, mouseX, mouseY);
      return;
    }
    graphics.renderTooltip(
        font,
        List.of(
            original,
            Component.translatable("tooltip.kaleidoscope_grilling.refrigerator.shift_sort")),
        Optional.empty(),
        mouseX,
        mouseY);
  }
}
