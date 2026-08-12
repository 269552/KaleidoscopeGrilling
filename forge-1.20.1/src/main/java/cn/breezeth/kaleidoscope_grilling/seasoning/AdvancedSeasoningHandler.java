package cn.breezeth.kaleidoscope_grilling.seasoning;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
import cn.breezeth.kaleidoscope_grilling.registry.ModEffects;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;


import java.util.List;
import java.util.UUID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public final class AdvancedSeasoningHandler {
  private static final int HEAVY_METAL_COOLDOWN_TICKS = 10 * 60 * 20;
  private static final UUID DRAGON_ID = UUID.fromString("3ca9fb43-40a8-48db-9a52-a98bef86dbe2");
  private static final String DRAGON_HEALED_KEY =
      KaleidoscopeGrilling.MOD_ID + ":dragon_blood_healed";

  public static void apply(LivingEntity entity, List<String> ingredients, int duration) {
    long totem = GrillingDataManager.seasoningCount(ingredients, "totem");
    long dragon = GrillingDataManager.seasoningCount(ingredients, "vitality");
    if (totem > 0 && entity.hasEffect(ModEffects.HEAVY_METAL_POISONING.get())) {
      ModAdvancements.heavyMetalBlocked(entity);
    } else if (totem > 0) {
      entity.addEffect(
          new MobEffectInstance(ModEffects.HEAVY_METAL.get(), duration, totem >= 4 ? 1 : 0));
    }
    if (dragon > 0)
      entity.addEffect(
          new MobEffectInstance(ModEffects.DRAGON_BLOOD.get(), duration, dragon >= 4 ? 1 : 0));
  }

  public static void onDeath(LivingDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!entity.hasEffect(ModEffects.HEAVY_METAL.get())
        || entity.hasEffect(ModEffects.HEAVY_METAL_POISONING.get())) return;
    event.setCanceled(true);
    entity.removeEffect(ModEffects.HEAVY_METAL.get());
    entity.setHealth(1.0F);
    entity.addEffect(
        new MobEffectInstance(ModEffects.HEAVY_METAL_POISONING.get(), HEAVY_METAL_COOLDOWN_TICKS));
    entity.level().broadcastEntityEvent(entity, (byte) 35);
  }

  public static void onLivingTick(LivingEvent.LivingTickEvent event) {
    LivingEntity entity = event.getEntity();
    if (entity.level().isClientSide) return;
    MobEffectInstance dragon = entity.getEffect(ModEffects.DRAGON_BLOOD.get());
    AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
    if (health == null) return;
    health.removeModifier(DRAGON_ID);
    if (dragon != null) {
      double amount = dragon.getAmplifier() > 0 ? 10.0D : 6.0D;
      health.addTransientModifier(
          new AttributeModifier(
              DRAGON_ID, "Grilling dragon blood", amount, AttributeModifier.Operation.ADDITION));
      double healedAmount = entity.getPersistentData().getDouble(DRAGON_HEALED_KEY);
      if (amount > healedAmount) entity.heal((float) (amount - healedAmount));
      entity.getPersistentData().putDouble(DRAGON_HEALED_KEY, amount);
    } else {
      entity.getPersistentData().remove(DRAGON_HEALED_KEY);
    }
    if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
  }

  private AdvancedSeasoningHandler() {}
}
