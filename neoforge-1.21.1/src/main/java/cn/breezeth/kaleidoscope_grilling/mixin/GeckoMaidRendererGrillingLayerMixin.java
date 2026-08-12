package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.MaidGrillingClientLayers;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.GeckoEntityMaidRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GeckoEntityMaidRenderer.class, remap = false)
abstract class GeckoMaidRendererGrillingLayerMixin {
  @Inject(method = "addAdditionGeckoEntityMaidRenderer", at = @At("TAIL"))
  private void kaleidoscopeGrilling$addHeldItemLayer(Context context, CallbackInfo ci) {
    MaidGrillingClientLayers.addGecko(
        (GeckoEntityMaidRenderer<? extends Mob>) (Object) this, context);
  }
}
