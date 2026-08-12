package cn.breezeth.kaleidoscope_grilling.food;

import cn.breezeth.kaleidoscope_grilling.HotFoodApi;
import cn.breezeth.kaleidoscope_grilling.registry.ModEffects;

import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;


import cn.breezeth.kaleidoscope_grilling.mixin.FoodDataAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class HotFoodHandler {
  private static final Map<LivingEntity, Map<Holder<MobEffect>, Integer>> EFFECTS_BEFORE_USE =
      new WeakHashMap<>();
  private static final Map<LivingEntity, Float> SATURATION_BEFORE_USE = new WeakHashMap<>();

  public static void onTooltip(ItemTooltipEvent event) {
    if (OilPotCompat.isOilPot(event.getItemStack())) {
      event
          .getToolTip()
          .removeIf(c -> c.getString().startsWith("数量：") || c.getString().startsWith("Count:"));
      String type = OilPotCompat.getType(event.getItemStack());
      int count = OilPotCompat.getCount(event.getItemStack());
      if (type.isEmpty()) {
        if (count > 0) {
          event
              .getToolTip()
              .add(
                  Component.translatable("tooltip.kaleidoscope_grilling.oil_pot.fat", count)
                      .withStyle(ChatFormatting.GRAY));
        } else {
          event
              .getToolTip()
              .add(
                  Component.translatable("tooltip.kaleidoscope_grilling.oil_pot.empty_tooltip")
                      .withStyle(ChatFormatting.GRAY));
        }
      } else {
        event
            .getToolTip()
            .add(
                Component.translatable("tooltip.kaleidoscope_grilling.oil_pot." + type, count)
                    .withStyle(ChatFormatting.GRAY));
      }
    }
    if (event.getEntity() != null
        && FoodState.isHot(event.getItemStack(), event.getEntity().level())) {
      event.getToolTip().add(Component.empty());
      CustomData data = event.getItemStack().get(DataComponents.CUSTOM_DATA);
      long until = data != null ? data.copyTag().getLong("HotUntil") : 0;
      long left = Math.max(1, (until - event.getEntity().level().getGameTime()) / 20);
      event
          .getToolTip()
          .add(
              Component.translatable(
                      "tooltip.kaleidoscope_grilling.hot_food",
                      left / 60,
                      String.format("%02d", left % 60))
                  .withStyle(ChatFormatting.RED));
    }
  }

  public static void onStart(LivingEntityUseItemEvent.Start event) {
    if (!event.getEntity().level().isClientSide
        && FoodState.isHot(event.getItem(), event.getEntity().level())) {
      EFFECTS_BEFORE_USE.put(event.getEntity(), snapshot(event.getEntity()));
      saturation(event.getEntity())
          .ifPresent(value -> SATURATION_BEFORE_USE.put(event.getEntity(), value));
    }
  }

  public static void onStop(LivingEntityUseItemEvent.Stop event) {
    if (!event.getEntity().level().isClientSide) {
      EFFECTS_BEFORE_USE.remove(event.getEntity());
      SATURATION_BEFORE_USE.remove(event.getEntity());
    }
  }

  public static void onFinish(LivingEntityUseItemEvent.Finish event) {
    if (event.getEntity().level().isClientSide) return;
    finishEarly(event.getItem(), event.getEntity());
  }

  /** Applies the normal finish-side heat and seasoning work for a deliberate early completion. */
  public static void finishEarly(ItemStack stack, LivingEntity entity) {
    Map<Holder<MobEffect>, Integer> before = EFFECTS_BEFORE_USE.remove(entity);
    Float saturationBefore = SATURATION_BEFORE_USE.remove(entity);
    boolean hot = FoodState.isHot(stack, entity.level());
    if (before != null && hot) doubleNewDurations(entity, before);
    if (saturationBefore != null && hot)
      multiplySaturationGain(entity, saturationBefore);
    FoodState.applySeasoning(stack, entity.level(), entity);
  }

  public static ItemStack finishNested(ItemStack stack, LivingEntity entity) {
    boolean hot = FoodState.isHot(stack, entity.level());
    Map<Holder<MobEffect>, Integer> before = hot ? snapshot(entity) : null;
    Float saturationBefore =
        hot && !entity.level().isClientSide ? saturation(entity).orElse(null) : null;
    ItemStack result = stack.finishUsingItem(entity.level(), entity);
    if (before != null) doubleNewDurations(entity, before);
    if (saturationBefore != null) multiplySaturationGain(entity, saturationBefore);
    FoodState.applySeasoning(stack, entity.level(), entity);
    return result;
  }

  public static void onSmelted(PlayerEvent.ItemSmeltedEvent event) {
    ItemStack result = event.getSmelting();
    if (!HotFoodConfig.ENABLE_SMELTED_FOOD.get()
        || result.isEmpty()
        || result.getItem().getFoodProperties(result, event.getEntity()) == null) return;
    HotFoodApi.makeHot(result, event.getEntity().level(), HotFoodConfig.SMELTED_FOOD_SECONDS.get());
  }

  private static Map<Holder<MobEffect>, Integer> snapshot(LivingEntity entity) {
    Map<Holder<MobEffect>, Integer> result = new HashMap<>();
    for (MobEffectInstance effect : entity.getActiveEffects())
      result.put(effect.getEffect(), effect.getDuration());
    return result;
  }

  private static void doubleNewDurations(
      LivingEntity entity, Map<Holder<MobEffect>, Integer> before) {
    for (MobEffectInstance effect : List.copyOf(entity.getActiveEffects())) {
      if (effect.getEffect().is(ModEffects.INVINCIBLE)) continue;
      int old = before.getOrDefault(effect.getEffect(), 0);
      if (effect.getDuration() <= old) continue;
      int duration = old + (effect.getDuration() - old) * 2;
      entity.removeEffect(effect.getEffect());
      entity.addEffect(
          new MobEffectInstance(
              effect.getEffect(),
              duration,
              effect.getAmplifier(),
              effect.isAmbient(),
              effect.isVisible(),
              effect.showIcon()));
    }
  }

  private static java.util.Optional<Float> saturation(LivingEntity entity) {
    if (!(entity instanceof Player player)) return java.util.Optional.empty();
    return java.util.Optional.of(
        ((FoodDataAccessor) player.getFoodData()).grilling$getSaturationLevel());
  }

  private static void multiplySaturationGain(LivingEntity entity, float before) {
    if (!(entity instanceof Player player)) return;
    float multiplier = HotFoodConfig.HOT_SATURATION_PERCENT.get() / 100.0F;
    if (multiplier <= 1.0F) return;
    FoodDataAccessor data = (FoodDataAccessor) player.getFoodData();
    float current = data.grilling$getSaturationLevel();
    float gained = Math.max(0.0F, current - before);
    if (gained <= 0.0F) return;
    data.grilling$setSaturationLevel(
        Math.min(data.grilling$getFoodLevel(), current + gained * (multiplier - 1.0F)));
  }

  private HotFoodHandler() {}
}
