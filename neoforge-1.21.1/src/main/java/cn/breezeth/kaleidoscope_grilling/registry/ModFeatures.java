package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.world.PepperTreeFeature;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
  public static final DeferredRegister<Feature<?>> FEATURES =
      DeferredRegister.create(BuiltInRegistries.FEATURE, KaleidoscopeGrilling.MOD_ID);
  public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> PEPPER_TREE =
      FEATURES.register("pepper_tree", () -> new PepperTreeFeature(NoneFeatureConfiguration.CODEC));

  private ModFeatures() {}
}
