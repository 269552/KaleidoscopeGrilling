package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.GrillingWirelessIOData;
import com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemWirelessIO.class, remap = false)
abstract class WirelessIOItemMixin {
  // The maid jar is mapped in userdev but keeps the SRG name in production.
  @Inject(
      method = {"appendHoverText", "m_7373_"},
      at = @At("HEAD"),
      cancellable = true,
      require = 1)
  private void kaleidoscopeGrilling$replaceGrillingDescription(
      ItemStack stack,
      @Nullable Level level,
      List<Component> tooltip,
      TooltipFlag flag,
      CallbackInfo ci) {
    if (GrillingWirelessIOData.isEnabled(stack)) ci.cancel();
  }
}
