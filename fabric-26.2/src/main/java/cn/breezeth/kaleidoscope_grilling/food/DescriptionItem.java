package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class DescriptionItem extends Item {
  private final String descriptionKey;

  public DescriptionItem(Properties properties, String descriptionKey) {
    super(properties);
    this.descriptionKey = descriptionKey;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.DARK_GRAY));
  }
}
