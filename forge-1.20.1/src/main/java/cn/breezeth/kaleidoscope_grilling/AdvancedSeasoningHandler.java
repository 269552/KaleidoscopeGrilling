package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.List;
import java.util.UUID;

public final class AdvancedSeasoningHandler {
    private static final UUID DRAGON_ID = UUID.fromString("3ca9fb43-40a8-48db-9a52-a98bef86dbe2");

    public static void apply(LivingEntity entity, List<String> ingredients, int duration) {
        long totem = GrillingDataManager.seasoningCount(ingredients, "totem");
        long dragon = GrillingDataManager.seasoningCount(ingredients, "vitality");
        if (totem > 0) entity.addEffect(new MobEffectInstance(ModEffects.HEAVY_METAL.get(), duration, totem >= 4 ? 1 : 0));
        if (dragon > 0) entity.addEffect(new MobEffectInstance(ModEffects.DRAGON_BLOOD.get(), duration, dragon >= 4 ? 1 : 0));
    }

    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.HEAVY_METAL.get())) return;
        event.setCanceled(true);
        entity.removeEffect(ModEffects.HEAVY_METAL.get());
        entity.setHealth(1.0F);
    }

    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance dragon = entity.getEffect(ModEffects.DRAGON_BLOOD.get());
        AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        health.removeModifier(DRAGON_ID);
        if (dragon != null) {
            double amount = dragon.getAmplifier() > 0 ? 10.0D : 6.0D;
            health.addTransientModifier(new AttributeModifier(DRAGON_ID, "Grilling dragon blood",
                    amount, AttributeModifier.Operation.ADDITION));
        } else if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private AdvancedSeasoningHandler() {}
}
