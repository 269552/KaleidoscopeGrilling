package cn.breezeth.kaleidoscope_grilling.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Base effect shared by the Fabric port. Cure filtering is wired through Fabric-side events. */
public final class GrillingMobEffect extends MobEffect {
  private final boolean curable;

  public GrillingMobEffect(MobEffectCategory category, int color) {
    this(category, color, true);
  }

  public GrillingMobEffect(MobEffectCategory category, int color, boolean curable) {
    super(category, color);
    this.curable = curable;
  }

  public boolean isCurable() {
    return curable;
  }
}
