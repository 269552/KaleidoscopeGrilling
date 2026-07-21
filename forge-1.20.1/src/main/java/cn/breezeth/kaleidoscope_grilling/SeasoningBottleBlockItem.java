package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class SeasoningBottleBlockItem extends BlockItem {
  public SeasoningBottleBlockItem(Block b, Properties p) {
    super(b, p);
  }

  @Override
  public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
    SeasoningTooltip.append(s, t);
  }
}
