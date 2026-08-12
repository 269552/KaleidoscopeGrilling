package cn.breezeth.kaleidoscope_grilling.seasoning;

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

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    int remaining = SeasoningData.MAX_USES - SeasoningData.getUses(stack);
    return remaining > 0 && remaining < SeasoningData.MAX_USES;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return Math.round(
        13.0F - (float) SeasoningData.getUses(stack) * 13.0F / (float) SeasoningData.MAX_USES);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    int remaining = SeasoningData.MAX_USES - SeasoningData.getUses(stack);
    return net.minecraft.util.Mth.hsvToRgb(
        Math.max(0.0F, (float) remaining) / (float) SeasoningData.MAX_USES / 3.0F, 1.0F, 1.0F);
  }
}
