package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class FlavorFoodItem extends Item {
  private final String tooltip;

  public FlavorFoodItem(Properties p, String t) {
    super(p);
    tooltip = t;
  }

  @Override
  public void appendHoverText(
      ItemStack s, @Nullable Level l, List<Component> lines, TooltipFlag f) {
    FoodTooltip.appendMaxim(lines, tooltip);
  }
}
