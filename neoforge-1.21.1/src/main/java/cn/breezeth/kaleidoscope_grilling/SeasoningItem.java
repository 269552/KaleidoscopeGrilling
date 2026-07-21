package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class SeasoningItem extends BlockItem {
  public SeasoningItem(Block block, Properties properties) {
    super(block, properties);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    SeasoningTooltip.appendFinished(stack, tooltip);
  }
}
