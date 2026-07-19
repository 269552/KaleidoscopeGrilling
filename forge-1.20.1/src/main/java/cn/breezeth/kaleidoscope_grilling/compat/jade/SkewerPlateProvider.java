package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.SkewerPlateBlockEntity;
import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum SkewerPlateProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof SkewerPlateBlockEntity plate)) return;
        var skewers = plate.copySkewers();
        if (!skewers.isEmpty()) JadeElements.appendItems(tooltip, skewers);
        tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_plate.count",
                skewers.size(), SkewerPlateBlockEntity.CAPACITY));
        tooltip.add(Component.translatable(skewers.isEmpty()
                ? "jade.kaleidoscope_grilling.skewer_plate.empty"
                : "jade.kaleidoscope_grilling.skewer_plate.take"));
        if (!skewers.isEmpty()) tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_plate.pack"));
    }

    @Override public ResourceLocation getUid() {
        return new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "skewer_plate");
    }
}
