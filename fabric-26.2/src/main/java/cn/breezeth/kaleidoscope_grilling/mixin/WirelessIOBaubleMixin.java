package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.GrillingWirelessIOData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.bauble.WirelessIOBauble;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WirelessIOBauble.class, remap = false)
abstract class WirelessIOBaubleMixin {
  @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
  private void kaleidoscopeGrilling$disableNativeTransfer(
      EntityMaid maid, ItemStack stack, CallbackInfo ci) {
    if (GrillingWirelessIOData.isEnabled(stack)) ci.cancel();
  }
}
