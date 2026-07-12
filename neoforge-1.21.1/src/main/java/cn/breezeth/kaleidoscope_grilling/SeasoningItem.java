package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public final class SeasoningItem extends Item {
    public SeasoningItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.seasoning_uses", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
        SeasoningTooltip.append(stack, tooltip);
    }
}
