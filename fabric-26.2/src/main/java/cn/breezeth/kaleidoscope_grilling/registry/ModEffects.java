package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.effect.GrillingMobEffect;
import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ModEffects {
  public static final RegistryRef<MobEffect> HEAVY_METAL =
      register("heavy_metal", new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0x70757D));
  public static final RegistryRef<MobEffect> HEAVY_METAL_POISONING =
      register("heavy_metal_poisoning", new GrillingMobEffect(MobEffectCategory.HARMFUL, 0x4F555D, false));
  public static final RegistryRef<MobEffect> DRAGON_BLOOD =
      register("dragon_blood", new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0x7D1738));
  public static final RegistryRef<MobEffect> NUMB =
      register("numb", new GrillingMobEffect(MobEffectCategory.NEUTRAL, 0xD8A22A));
  public static final RegistryRef<MobEffect> INVINCIBLE =
      register("invincible", new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0xFFD44A));

  private static RegistryRef<MobEffect> register(String name, MobEffect effect) {
    Identifier id = Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name);
    ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id);
    return RegistryRef.of(Registry.register(BuiltInRegistries.MOB_EFFECT, key, effect));
  }

  public static void init() {}

  private ModEffects() {}
}
