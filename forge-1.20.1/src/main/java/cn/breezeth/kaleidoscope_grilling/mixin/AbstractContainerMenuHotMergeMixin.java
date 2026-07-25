package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.AdvancedRackMenu;
import cn.breezeth.kaleidoscope_grilling.FoodState;
import cn.breezeth.kaleidoscope_grilling.HotFoodMerge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuHotMergeMixin {
  @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
  private void grilling$mergeHotFood(Player player, int button, CallbackInfoReturnable<Boolean> cir) {
    if (!HotFoodMerge.isMergeButton(button)
        || !(player instanceof ServerPlayer)
        || (Object) this instanceof AdvancedRackMenu) return;
    AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
    int slotId = HotFoodMerge.slotFromMenuButton(button);
    if (slotId < 0 || slotId >= menu.slots.size()) {
      cir.setReturnValue(false);
      return;
    }
    Slot slot = menu.slots.get(slotId);
    int merged = FoodState.mergeHot(slot.getItem(), menu.getCarried(), player.level());
    if (merged > 0) {
      slot.setChanged();
      menu.setCarried(menu.getCarried());
      menu.broadcastChanges();
      cir.setReturnValue(true);
    } else cir.setReturnValue(false);
  }
}
