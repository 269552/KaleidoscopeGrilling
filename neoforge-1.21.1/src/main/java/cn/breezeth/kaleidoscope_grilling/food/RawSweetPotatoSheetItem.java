package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class RawSweetPotatoSheetItem extends Item {
  public RawSweetPotatoSheetItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.raw_sweet_potato_sheet")
            .withStyle(ChatFormatting.DARK_GRAY));
  }
}
