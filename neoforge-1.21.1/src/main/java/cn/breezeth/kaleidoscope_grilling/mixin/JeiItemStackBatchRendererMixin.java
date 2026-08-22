package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.skewer.SkewerGuiIconCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Covers JEI's optimized ingredient-list path, which bypasses GuiGraphics.renderItem. */
@Pseudo
@Mixin(targets = "mezz.jei.library.render.batch.ItemStackBatchRenderer", remap = false)
public abstract class JeiItemStackBatchRendererMixin {
  @Inject(
      method = "renderItem",
      at = @At("HEAD"),
      cancellable = true,
      remap = false)
  private void grilling$renderSkewerIcon(
      GuiGraphics graphics,
      ItemRenderer itemRenderer,
      BakedModel model,
      ItemStack stack,
      int x,
      int y,
      CallbackInfo ci) {
    if (SkewerGuiIconCache.render(graphics, stack, x, y)) ci.cancel();
  }
}
