package cn.breezeth.kaleidoscope_grilling.compat.jade;

import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElementHelper;

final class JadeElements {
    static void appendItems(ITooltip tooltip, Iterable<ItemStack> stacks) {
        IElementHelper helper = IElementHelper.get();
        boolean first = true;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            if (!first) tooltip.append(helper.spacer(2, 1));
            ItemStack icon = stack.copy();
            icon.setCount(1);
            tooltip.append(helper.smallItem(icon));
            first = false;
        }
    }

    private JadeElements() {}
}
