package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FunctionalBlockItem extends BlockItem {
    private final String usageKey;
    private final String requirementKey;

    public FunctionalBlockItem(Block block, Properties properties, String usageKey, String requirementKey) {
        super(block, properties);
        this.usageKey = usageKey;
        this.requirementKey = requirementKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(usageKey).withStyle(ChatFormatting.DARK_GRAY));
        if (requirementKey != null) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable(requirementKey).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
