package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.ModItems;
import cn.breezeth.kaleidoscope_grilling.SkewerPlateItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class SkewerPlateEatingParticlesMixin {
  @ModifyVariable(method = "spawnItemParticles", at = @At("HEAD"), argsOnly = true, ordinal = 0)
  private ItemStack kaleidoscopeGrilling$useSkewerParticles(ItemStack stack) {
    if (!stack.is(ModItems.SKEWER_PLATE.get())) return stack;
    return SkewerPlateItem.particleStack(stack, (LivingEntity) (Object) this);
  }
}
