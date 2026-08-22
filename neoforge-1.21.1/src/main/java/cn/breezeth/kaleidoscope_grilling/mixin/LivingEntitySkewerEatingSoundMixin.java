package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
    if (!SkewerRecipes.usesCustomEating(self.getUseItem())) {
      entity.playSound(sound, volume, pitch);
      return;
    }
    if (!self.level().isClientSide && self instanceof Player player) {
      self.level()
          .playSound(
              player,
              self.getX(),
              self.getY(),
              self.getZ(),
              sound,
              self.getSoundSource(),
              volume,
              pitch);
    }
  }

  @Redirect(
      method =
          "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
  private void grilling$broadcastSkewerCompletionSound(
      Level level,
      Player excluded,
      double x,
      double y,
      double z,
      SoundEvent sound,
      SoundSource source,
      float volume,
      float pitch) {
    LivingEntity self = (LivingEntity) (Object) this;
    level.playSound(
        SkewerRecipes.usesCustomEating(self.getUseItem()) && self instanceof Player player
            ? player
            : excluded,
        x,
        y,
        z,
        sound,
        source,
        volume,
        pitch);
  }
}
