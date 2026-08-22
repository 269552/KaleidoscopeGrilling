package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps server-broadcast eating fragments away from the eater's own first-person camera. */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySkewerEatingParticlesServerMixin {
  @Redirect(
      method = "spawnItemParticles",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
  private int grilling$sendSkewerParticlesToObservers(
      ServerLevel level,
      ParticleOptions particle,
      double x,
      double y,
      double z,
      int count,
      double xOffset,
      double yOffset,
      double zOffset,
      double speed) {
    LivingEntity self = (LivingEntity) (Object) this;
    if (!(self instanceof ServerPlayer eater)
        || !SkewerRecipes.usesCustomEating(self.getUseItem())) {
      return level.sendParticles(
          particle, x, y, z, count, xOffset, yOffset, zOffset, speed);
    }

    int recipients = 0;
    for (ServerPlayer observer : level.players()) {
      if (observer == eater) continue;
      if (level.sendParticles(
          observer,
          particle,
          false,
          x,
          y,
          z,
          count,
          xOffset,
          yOffset,
          zOffset,
          speed)) recipients++;
    }
    return recipients;
  }
}
