package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.compat.ordertocook.RefrigeratorSkewerSorter;
import cn.breezeth.ordertocook.screen.RefrigeratorScreenHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RefrigeratorScreenHandler.class)
abstract class RefrigeratorScreenHandlerSkewerSortMixin {
  @Shadow @Final private Container inventory;
  @Shadow public abstract int containerSlotCount();

  @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true, require = 0)
  private void grilling$sortSkewers(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
    if (player.level().isClientSide()
        || (id != RefrigeratorSkewerSorter.NORMAL_SORT_ID && id != RefrigeratorSkewerSorter.FULL_SORT_ID)) return;
    RefrigeratorSkewerSorter.compact(
        inventory, containerSlotCount(), player.level(), id == RefrigeratorSkewerSorter.FULL_SORT_ID);
    cir.setReturnValue(true);
  }
}
