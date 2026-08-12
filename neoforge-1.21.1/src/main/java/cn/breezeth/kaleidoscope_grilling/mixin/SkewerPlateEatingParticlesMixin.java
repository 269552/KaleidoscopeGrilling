package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerPlateItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class SkewerPlateEatingParticlesMixin {
  @Inject(method = "spawnItemParticles", at = @At("HEAD"), cancellable = true)
  private void kaleidoscopeGrilling$hideMultiBiteSkewerParticles(
      ItemStack stack, int amount, CallbackInfo ci) {
    if (stack.getItem() instanceof MultiBiteSkewerItem) ci.cancel();
  }

  @ModifyVariable(method = "spawnItemParticles", at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private ItemStack kaleidoscopeGrilling$useSkewerParticles(ItemStack stack) {
    if (!stack.is(ModItems.SKEWER_PLATE.get())) return stack;
    return SkewerPlateItem.particleStack(stack, (LivingEntity) (Object) this);
  }
}
