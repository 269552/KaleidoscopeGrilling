package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityHotFoodExpiryMixin {
  @Inject(method = "tick", at = @At("HEAD"))
  private void grilling$clearExpiredHeat(CallbackInfo ci) {
    ItemEntity itemEntity = (ItemEntity) (Object) this;
    if (!itemEntity.level().isClientSide && itemEntity.tickCount % 20 == 0)
      FoodState.clearExpired(itemEntity.getItem(), itemEntity.level());
  }
}
