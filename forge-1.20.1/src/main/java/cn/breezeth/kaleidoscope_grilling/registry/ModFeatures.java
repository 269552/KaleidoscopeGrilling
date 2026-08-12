package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.world.PepperTreeFeature;


import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
  public static final DeferredRegister<Feature<?>> FEATURES =
      DeferredRegister.create(ForgeRegistries.FEATURES, KaleidoscopeGrilling.MOD_ID);
  public static final RegistryObject<Feature<NoneFeatureConfiguration>> PEPPER_TREE =
      FEATURES.register("pepper_tree", () -> new PepperTreeFeature(NoneFeatureConfiguration.CODEC));

  private ModFeatures() {}
}
