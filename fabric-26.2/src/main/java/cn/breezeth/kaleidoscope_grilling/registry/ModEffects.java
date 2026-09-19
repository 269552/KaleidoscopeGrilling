package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.effect.GrillingMobEffect;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
  public static final DeferredRegister<MobEffect> EFFECTS =
      DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, KaleidoscopeGrilling.MOD_ID);
  public static final DeferredHolder<MobEffect, MobEffect> HEAVY_METAL =
      EFFECTS.register(
          "heavy_metal", () -> new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0x70757D));
  public static final DeferredHolder<MobEffect, MobEffect> HEAVY_METAL_POISONING =
      EFFECTS.register(
          "heavy_metal_poisoning",
          () -> new GrillingMobEffect(MobEffectCategory.HARMFUL, 0x4F555D, false));
  public static final DeferredHolder<MobEffect, MobEffect> DRAGON_BLOOD =
      EFFECTS.register(
          "dragon_blood", () -> new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0x7D1738));
  public static final DeferredHolder<MobEffect, MobEffect> NUMB =
      EFFECTS.register("numb", () -> new GrillingMobEffect(MobEffectCategory.NEUTRAL, 0xD8A22A));
  public static final DeferredHolder<MobEffect, MobEffect> INVINCIBLE =
      EFFECTS.register(
          "invincible", () -> new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0xFFD44A));

  private ModEffects() {}
}
