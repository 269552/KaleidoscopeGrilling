package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
  public static final DeferredRegister<MobEffect> EFFECTS =
      DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, KaleidoscopeGrilling.MOD_ID);
  public static final RegistryObject<MobEffect> HEAVY_METAL =
      EFFECTS.register(
          "heavy_metal", () -> new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0x70757D));
  public static final RegistryObject<MobEffect> HEAVY_METAL_POISONING =
      EFFECTS.register(
          "heavy_metal_poisoning",
          () -> new GrillingMobEffect(MobEffectCategory.HARMFUL, 0x4F555D));
  public static final RegistryObject<MobEffect> DRAGON_BLOOD =
      EFFECTS.register(
          "dragon_blood", () -> new GrillingMobEffect(MobEffectCategory.BENEFICIAL, 0x7D1738));
  public static final RegistryObject<MobEffect> NUMB =
      EFFECTS.register("numb", () -> new GrillingMobEffect(MobEffectCategory.NEUTRAL, 0xD8A22A));

  private ModEffects() {}
}
