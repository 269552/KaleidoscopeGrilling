package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class OilPressApi {
    public static boolean addProgress(Level level, BlockPos pressPos, int amount) {
        return level != null && !level.isClientSide
                && level.getBlockEntity(pressPos) instanceof OilPressBlockEntity press
                && press.addProgress(amount);
    }

    public static int requiredProgress() { return OilPressBlockEntity.REQUIRED_PROGRESS; }
    private OilPressApi() {}
}
