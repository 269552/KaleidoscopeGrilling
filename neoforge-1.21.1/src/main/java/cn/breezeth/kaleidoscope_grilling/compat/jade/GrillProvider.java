package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.GrillBlock;
import cn.breezeth.kaleidoscope_grilling.GrillBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

enum GrillProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof GrillBlockEntity grill)) return;
        List<ItemStack> skewers = new ArrayList<>();
        for (int i = 0; i < grill.getContainerSize(); i++) {
            if (!grill.getItem(i).isEmpty()) skewers.add(grill.getItem(i));
        }
        if (!skewers.isEmpty()) {
            JadeElements.appendItems(tooltip, skewers);
            tooltip.add(Component.translatable(statusKey(accessor, grill), grill.getFlips(), 4));
        } else {
            tooltip.add(Component.translatable(accessor.getBlockState().getValue(GrillBlock.LIT)
                    ? "jade.kaleidoscope_grilling.grill.empty"
                    : "jade.kaleidoscope_grilling.grill.need_heat"));
        }
    }

    private static String statusKey(BlockAccessor accessor, GrillBlockEntity grill) {
        if (!accessor.getBlockState().getValue(GrillBlock.LIT)) return "jade.kaleidoscope_grilling.grill.need_heat";
        if (grill.getPhase() == 0) return "jade.kaleidoscope_grilling.grill.need_oil";
        if (grill.getPhase() == 1) return grill.getFlipCooldown() > 0
                ? "jade.kaleidoscope_grilling.grill.flipping" : "jade.kaleidoscope_grilling.grill.need_flip";
        if (grill.getPhase() == 2) return grill.isSeasoned()
                ? "jade.kaleidoscope_grilling.grill.ready" : "jade.kaleidoscope_grilling.grill.need_seasoning";
        return "jade.kaleidoscope_grilling.grill.burning";
    }

    @Override public ResourceLocation getUid() { return GrillingJadePlugin.GRILL; }
}
