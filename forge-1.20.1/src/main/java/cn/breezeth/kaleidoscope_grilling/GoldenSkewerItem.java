package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GoldenSkewerItem extends SkewerItem {
  public GoldenSkewerItem(Properties properties) {
    super(properties, "tooltip.kaleidoscope_grilling.grilled_golden_skewer.maxim", new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "invincible"), 200);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(stack, level, entity);
    if (level instanceof ServerLevel server) {
      server.playSound(null, entity.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.15F);
      for (int i = 0; i < 18; i++) {
        double angle = Math.PI * 2.0 * i / 18.0;
        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX() + Math.cos(angle) * 0.65, entity.getY() + 0.25 + i * 0.06, entity.getZ() + Math.sin(angle) * 0.65, 1, 0, 0.02, 0, 0.02);
      }
    }
    return result;
  }
}
