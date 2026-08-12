package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.MultiBiteSkewerItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySkewerEatingSoundMixin {
  @Redirect(
      method = "triggerItemUseEffects",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
  private void grilling$suppressVanillaSkewerEatingSound(
      LivingEntity entity, SoundEvent sound, float volume, float pitch) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (!(self.getUseItem().getItem() instanceof MultiBiteSkewerItem))
      entity.playSound(sound, volume, pitch);
  }
}
