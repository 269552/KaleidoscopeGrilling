package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.skewer.SkewerGuiIconCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces JEI's direct item-model rendering with the authored skewer GUI icon. */
@Pseudo
@Mixin(targets = "mezz.jei.library.render.ItemStackRenderer", remap = false)
public abstract class JeiItemStackRendererMixin {
  @Inject(
      method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/item/ItemStack;II)V",
      at = @At("HEAD"),
      cancellable = true,
      remap = false)
  private void grilling$renderSkewerIcon(
      GuiGraphics graphics, ItemStack stack, int x, int y, CallbackInfo ci) {
    if (SkewerGuiIconCache.render(graphics, stack, x, y)) ci.cancel();
  }
}
