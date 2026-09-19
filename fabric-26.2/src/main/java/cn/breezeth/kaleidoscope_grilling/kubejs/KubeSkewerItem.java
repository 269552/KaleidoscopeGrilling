package cn.breezeth.kaleidoscope_grilling.kubejs;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** A KubeJS-registered item that uses Grilling's eating and generated-model behavior. */
public final class KubeSkewerItem extends MultiBiteSkewerItem {
  private final boolean generatedModel;

  KubeSkewerItem(
      Properties properties,
      @Nullable ResourceLocation effect,
      int effectTicks,
      AnimationProfile animation,
      boolean generatedModel) {
    super(properties, null, effect, effectTicks, animation);
    this.generatedModel = generatedModel;
  }

  public boolean usesGeneratedModel() {
    return generatedModel;
  }
}
