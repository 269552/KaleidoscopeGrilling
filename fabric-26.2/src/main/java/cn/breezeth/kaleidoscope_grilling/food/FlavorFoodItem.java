package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public final class FlavorFoodItem extends Item {
  private final String tooltip;

  public FlavorFoodItem(Properties p, String t) {
    super(p);
    tooltip = t;
  }

  @Override
  public @Nullable FoodProperties getFoodProperties(
      ItemStack stack, @Nullable LivingEntity entity) {
    return CuisineQualitySupport.foodProperties(stack, super.getFoodProperties(stack, entity));
  }

  @Override
  public void appendHoverText(ItemStack s, TooltipContext c, List<Component> lines, TooltipFlag f) {
    FoodTooltip.appendMaxim(lines, tooltip);
    CuisineQualitySupport.appendTooltip(s, lines);
  }
}
