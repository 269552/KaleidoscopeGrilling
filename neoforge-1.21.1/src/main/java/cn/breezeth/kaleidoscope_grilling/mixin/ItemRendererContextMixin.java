package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.SkewerItemRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererContextMixin {
  private static final String RENDER_STATIC =
      "renderStatic(Lnet/minecraft/world/item/ItemStack;"
          + "Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;"
          + "Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V";

  @Inject(method = RENDER_STATIC, at = @At("HEAD"))
  private void grilling$pushContext(
      ItemStack stack,
      ItemDisplayContext context,
      int light,
      int overlay,
      PoseStack pose,
      MultiBufferSource buffers,
      Level level,
      int seed,
      CallbackInfo ci) {
    SkewerItemRenderContext.push(context);
  }

  @Inject(method = RENDER_STATIC, at = @At("RETURN"))
  private void grilling$popContext(
      ItemStack stack,
      ItemDisplayContext context,
      int light,
      int overlay,
      PoseStack pose,
      MultiBufferSource buffers,
      Level level,
      int seed,
      CallbackInfo ci) {
    SkewerItemRenderContext.pop();
  }
}
