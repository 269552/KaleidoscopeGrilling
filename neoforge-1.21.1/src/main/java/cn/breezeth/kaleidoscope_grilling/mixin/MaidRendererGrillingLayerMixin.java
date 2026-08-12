package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.MaidGrillingClientLayers;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityMaidRenderer.class, remap = false)
abstract class MaidRendererGrillingLayerMixin {
  @Inject(method = "addAdditionMaidLayer", at = @At("TAIL"))
  private void kaleidoscopeGrilling$addHeldItemLayer(Context context, CallbackInfo ci) {
    MaidGrillingClientLayers.addVanilla((EntityMaidRenderer) (Object) this, context);
  }
}
