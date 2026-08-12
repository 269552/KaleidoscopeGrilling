package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public final class InvincibleHandler {
  private static final String FEEDBACK_TICK = "KaleidoscopeGrillingInvincibleFeedback";

  public static void onDamage(LivingAttackEvent event) {
    LivingEntity entity = event.getEntity();
    if (!entity.hasEffect(ModEffects.INVINCIBLE.get()) || event.getSource().is(DamageTypes.GENERIC_KILL)) return;
    event.setCanceled(true);
    long now = entity.level().getGameTime();
    long previous = entity.getPersistentData().getLong(FEEDBACK_TICK);
    if (now - previous < 6) return;
    entity.getPersistentData().putLong(FEEDBACK_TICK, now);
    if (entity.level() instanceof ServerLevel level) {
      level.playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.7F, 1.25F);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + entity.getBbHeight() * 0.55, entity.getZ(), 8, 0.35, 0.45, 0.35, 0.08);
      level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + entity.getBbHeight() * 0.55, entity.getZ(), 4, 0.25, 0.35, 0.25, 0.03);
    }
  }

  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    var player = event.player;
    if (event.phase != TickEvent.Phase.END || player.tickCount % 10 != 0 || !player.hasEffect(ModEffects.INVINCIBLE.get()) || !(player.level() instanceof ServerLevel level)) return;
    double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX() + Math.cos(angle) * 0.45, player.getY() + 0.3 + player.getRandom().nextDouble() * 1.4, player.getZ() + Math.sin(angle) * 0.45, 1, 0, 0, 0, 0);
  }

  private InvincibleHandler() {}
}
