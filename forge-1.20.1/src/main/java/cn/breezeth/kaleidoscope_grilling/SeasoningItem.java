package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public final class SeasoningItem extends BlockItem {
    public SeasoningItem(Block block, Properties properties) { super(block, properties); }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.seasoning_uses", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
        SeasoningTooltip.append(stack, tooltip);
    }
}
