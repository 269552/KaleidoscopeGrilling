package cn.breezeth.kaleidoscope_grilling;

import java.util.Set;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

public final class GrillingMobEffect extends MobEffect {
  private final boolean curable;

  public GrillingMobEffect(MobEffectCategory category, int color) {
    this(category, color, true);
  }

  public GrillingMobEffect(MobEffectCategory category, int color, boolean curable) {
    super(category, color);
    this.curable = curable;
  }

  @Override
  public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
    if (curable) super.fillEffectCures(cures, effectInstance);
  }
}
