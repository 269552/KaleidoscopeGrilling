package cn.breezeth.kaleidoscope_grilling;


import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class AdvancedSeasoningHandler {
  private static final int HEAVY_METAL_COOLDOWN_TICKS = 10 * 60 * 20;
  private static final ResourceLocation DRAGON_ID =
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "dragon_blood_health");
  private static final String DRAGON_HEALED_KEY =
      KaleidoscopeGrilling.MOD_ID + ":dragon_blood_healed";

  public static void apply(LivingEntity entity, List<String> ingredients, int duration) {
    long totem = GrillingDataManager.seasoningCount(ingredients, "totem");
    long dragon = GrillingDataManager.seasoningCount(ingredients, "vitality");
    if (totem > 0 && entity.hasEffect(ModEffects.HEAVY_METAL_POISONING)) {
      ModAdvancements.heavyMetalBlocked(entity);
    } else if (totem > 0) {
      entity.addEffect(new MobEffectInstance(ModEffects.HEAVY_METAL, duration, totem >= 4 ? 1 : 0));
    }
    if (dragon > 0)
      entity.addEffect(
          new MobEffectInstance(ModEffects.DRAGON_BLOOD, duration, dragon >= 4 ? 1 : 0));
  }

  public static void onDeath(LivingDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!entity.hasEffect(ModEffects.HEAVY_METAL)
        || entity.hasEffect(ModEffects.HEAVY_METAL_POISONING)) return;
    event.setCanceled(true);
    entity.removeEffect(ModEffects.HEAVY_METAL);
    entity.setHealth(1.0F);
    entity.addEffect(
        new MobEffectInstance(ModEffects.HEAVY_METAL_POISONING, HEAVY_METAL_COOLDOWN_TICKS));
    entity.level().broadcastEntityEvent(entity, (byte) 35);
  }

  public static void onEntityTick(EntityTickEvent.Post event) {
    Entity raw = event.getEntity();
    if (!(raw instanceof LivingEntity entity)) return;
    if (entity.level().isClientSide) return;
    MobEffectInstance dragon = entity.getEffect(ModEffects.DRAGON_BLOOD);
    AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
    if (health == null) return;
    health.removeModifier(DRAGON_ID);
    if (dragon != null) {
      double amount = dragon.getAmplifier() > 0 ? 10.0D : 6.0D;
      health.addTransientModifier(
          new AttributeModifier(DRAGON_ID, amount, AttributeModifier.Operation.ADD_VALUE));
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
