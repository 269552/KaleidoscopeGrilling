package cn.breezeth.kaleidoscope_grilling.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackMenu;
import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodMerge;
import cn.breezeth.kaleidoscope_grilling.rack.RackKeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenHotMergeMixin {
  private static final ResourceLocation MERGE_BORDER =
      ResourceLocation.tryParse(
          "kaleidoscope_grilling:textures/gui/hot_food_merge_border.png");

  @Shadow protected AbstractContainerMenu menu;

  @Inject(method = "renderSlot", at = @At("TAIL"))
  private void grilling$drawHotMergeHint(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null
        || !RackKeyHandler.isCapsHeld()
        || menu instanceof AdvancedRackMenu
        || menu.getCarried().isEmpty()
        || slot.getItem().isEmpty()
        || minecraft.player == null
        || !(HotFoodMerge.isAllowedClientTarget(
                minecraft.player,
                menu,
                slot,
                ((AbstractContainerScreen<?>)(Object)this).getTitle())
            || (minecraft.screen instanceof CreativeModeInventoryScreen
                && HotFoodMerge.isAllowedCreativeInventoryTarget(
                    minecraft.player, menu, slot)))
        || slot.getItem().getCount() >= slot.getItem().getMaxStackSize()
        || !FoodState.canMergeHot(slot.getItem(), menu.getCarried(), minecraft.level)) return;
    graphics.pose().pushPose();
    graphics.pose().translate(0.0F, 0.0F, 300.0F);
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    graphics.blit(MERGE_BORDER, slot.x, slot.y, 16, 16, 0.0F, 0.0F, 64, 64, 64, 64);
    graphics.flush();
    RenderSystem.disableBlend();
    graphics.pose().popPose();
  }

  @Inject(
      method = "render",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;"
                      + "renderFloatingItem(Lnet/minecraft/client/gui/GuiGraphics;"
                      + "Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
              ordinal = 0))
  private void grilling$flushSlotLayersBeforeCarriedItem(
      GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
    graphics.flush();
  }
}
