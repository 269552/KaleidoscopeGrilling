package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class HotFoodHandler {
    private static final Map<LivingEntity, Map<MobEffect, Integer>> EFFECTS_BEFORE_USE = new WeakHashMap<>();

    public static void onTooltip(ItemTooltipEvent event) {
        if (OilPotCompat.isOilPot(event.getItemStack())) {
            event.getToolTip().removeIf(c -> c.getString().startsWith("数量：") || c.getString().startsWith("Count:"));
            String type=OilPotCompat.getType(event.getItemStack());int count=OilPotCompat.getCount(event.getItemStack());
            if(type.isEmpty()){if(count>0){event.getToolTip().add(Component.translatable("tooltip.kaleidoscope_grilling.oil_pot.fat",count).withStyle(ChatFormatting.GRAY));}else{event.getToolTip().add(Component.translatable("tooltip.kaleidoscope_grilling.oil_pot.empty_tooltip").withStyle(ChatFormatting.GRAY));}}else{event.getToolTip().add(Component.translatable("tooltip.kaleidoscope_grilling.oil_pot."+type,count).withStyle(ChatFormatting.GRAY));}
        }
        if (event.getEntity() != null && FoodState.isHot(event.getItemStack(), event.getEntity().level())) {
            event.getToolTip().add(Component.empty());
            long until=event.getItemStack().getTag().getLong("HotUntil");long left=Math.max(1,(until-event.getEntity().level().getGameTime())/20);
            event.getToolTip().add(Component.translatable("tooltip.kaleidoscope_grilling.hot_food",left/60,String.format("%02d",left%60)).withStyle(ChatFormatting.RED));
        }
    }

    public static void onStart(LivingEntityUseItemEvent.Start event) {
        if (!event.getEntity().level().isClientSide && FoodState.isHot(event.getItem(), event.getEntity().level())) EFFECTS_BEFORE_USE.put(event.getEntity(), snapshot(event.getEntity()));
    }
    public static void onStop(LivingEntityUseItemEvent.Stop event) { if (!event.getEntity().level().isClientSide) EFFECTS_BEFORE_USE.remove(event.getEntity()); }
    public static void onFinish(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity().level().isClientSide) return;
        Map<MobEffect, Integer> before = EFFECTS_BEFORE_USE.remove(event.getEntity());
        if (before != null && FoodState.isHot(event.getItem(), event.getEntity().level())) doubleNewDurations(event.getEntity(), before);
        FoodState.applySeasoning(event.getItem(), event.getEntity().level(), event.getEntity());
    }
    public static ItemStack finishNested(ItemStack stack, LivingEntity entity) {
        boolean hot = FoodState.isHot(stack, entity.level());
        Map<MobEffect, Integer> before = hot ? snapshot(entity) : null;
        ItemStack result = stack.finishUsingItem(entity.level(), entity);
        if (before != null) doubleNewDurations(entity, before);
        return result;
    }
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
        ItemStack result = event.getSmelting();
        if (!HotFoodConfig.ENABLE_SMELTED_FOOD.get() || result.isEmpty() || result.getItem().getFoodProperties(result, event.getEntity()) == null) return;
        HotFoodApi.makeHot(result, event.getEntity().level(), HotFoodConfig.SMELTED_FOOD_SECONDS.get());
    }
    private static Map<MobEffect, Integer> snapshot(LivingEntity entity) {
        Map<MobEffect, Integer> result = new HashMap<>();
        for (MobEffectInstance effect : entity.getActiveEffects()) result.put(effect.getEffect(), effect.getDuration());
        return result;
    }
    private static void doubleNewDurations(LivingEntity entity, Map<MobEffect, Integer> before) {
        for (MobEffectInstance effect : List.copyOf(entity.getActiveEffects())) {
            int old = before.getOrDefault(effect.getEffect(), 0);
            if (effect.getDuration() <= old) continue;
            int duration = old + (effect.getDuration() - old) * 2;
            entity.removeEffect(effect.getEffect());
            entity.addEffect(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
    }
    private HotFoodHandler() {}
}
