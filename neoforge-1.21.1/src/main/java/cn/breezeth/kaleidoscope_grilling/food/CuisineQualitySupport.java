package cn.breezeth.kaleidoscope_grilling.food;

import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.Quality;
import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.QualityUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class CuisineQualitySupport {
  static @Nullable FoodProperties foodProperties(
      ItemStack stack, @Nullable FoodProperties properties) {
    if (properties == null || !QualityUtils.hasQuality(stack)) return properties;
    return QualityUtils.modifyFoodProperties(properties, QualityUtils.getQuality(stack));
  }

  static int effectDuration(ItemStack stack, int duration) {
    if (!QualityUtils.hasQuality(stack)) return duration;
    Quality quality = QualityUtils.getQuality(stack);
    return (int) Math.round(duration * quality.getRatio());
  }

  static void appendTooltip(ItemStack stack, List<Component> lines) {
    if (QualityUtils.hasQuality(stack)) {
      lines.add(QualityUtils.getQuality(stack).getTooltip());
    }
  }

  private CuisineQualitySupport() {}
}
