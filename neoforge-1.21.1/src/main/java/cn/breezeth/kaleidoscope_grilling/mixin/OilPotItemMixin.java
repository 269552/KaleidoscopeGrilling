package cn.breezeth.kaleidoscope_grilling.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem")
public abstract class OilPotItemMixin {
  @ModifyConstant(method = "setOilCount", constant = @Constant(intValue = 256), remap = false)
  private static int grilling$capMaxOilCount(int value) {
    return 64;
  }

  @Inject(method = "getOilCount", at = @At("RETURN"), cancellable = true, remap = false)
  private static void grilling$capGetOilCount(
      CallbackInfoReturnable<Integer> cir) {
    if (cir.getReturnValue() > 64) {
      cir.setReturnValue(64);
    }
  }
}
