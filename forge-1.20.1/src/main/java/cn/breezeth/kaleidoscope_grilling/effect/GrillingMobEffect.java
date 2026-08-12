package cn.breezeth.kaleidoscope_grilling.effect;

import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

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
  public List<ItemStack> getCurativeItems() {
    return curable ? super.getCurativeItems() : List.of();
  }
}
