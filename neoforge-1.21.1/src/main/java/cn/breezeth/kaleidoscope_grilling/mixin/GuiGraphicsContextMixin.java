package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.SkewerGuiIconCache;
import cn.breezeth.kaleidoscope_grilling.SkewerItemRenderContext;
import cn.breezeth.kaleidoscope_grilling.HotFoodGuiBadge;
import cn.breezeth.kaleidoscope_grilling.SkewerOutlineRender;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsContextMixin {
  @Unique
  private static final ThreadLocal<Integer> grilling$activeGuiRenders =
      ThreadLocal.withInitial(() -> 0);

  private static final String RENDER_ITEM =
      "renderItem(Lnet/minecraft/world/entity/LivingEntity;"
          + "Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V";

  @Inject(method = RENDER_ITEM, at = @At("HEAD"), cancellable = true)
  private void grilling$pushGuiContext(
      LivingEntity entity,
      Level level,
      ItemStack stack,
      int x,
      int y,
      int seed,
      int depth,
      CallbackInfo ci) {
    SkewerItemRenderContext.push(ItemDisplayContext.GUI);
    if (SkewerGuiIconCache.render((GuiGraphics) (Object) this, stack, x, y)) {
      SkewerItemRenderContext.pop();
      ci.cancel();
      return;
    }
    grilling$activeGuiRenders.set(grilling$activeGuiRenders.get() + 1);
  }

  @Inject(method = RENDER_ITEM, at = @At("RETURN"))
  private void grilling$popGuiContext(
      LivingEntity entity,
      Level level,
      ItemStack stack,
      int x,
      int y,
      int seed,
      int depth,
      CallbackInfo ci) {
    int active = grilling$activeGuiRenders.get();
    if (active <= 0) return;
    if (!SkewerItemRenderContext.isCapturing() && !SkewerOutlineRender.isActive())
      HotFoodGuiBadge.render((GuiGraphics) (Object) this, stack, level, x, y);
    if (active == 1) grilling$activeGuiRenders.remove();
    else grilling$activeGuiRenders.set(active - 1);
    SkewerItemRenderContext.pop();
  }
}
