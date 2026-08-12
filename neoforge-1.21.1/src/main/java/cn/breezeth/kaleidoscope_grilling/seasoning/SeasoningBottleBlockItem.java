package cn.breezeth.kaleidoscope_grilling.seasoning;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class SeasoningBottleBlockItem extends BlockItem {
  public SeasoningBottleBlockItem(Block b, Properties p) {
    super(b, p);
  }

  @Override
  public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
    SeasoningTooltip.appendUnfinished(s, t);
  }
}
