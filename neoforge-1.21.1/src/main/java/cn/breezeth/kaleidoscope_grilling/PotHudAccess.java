package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface PotHudAccess {
    int grilling$getStatus();
    List<ItemStack> grilling$getInputs();
    boolean grilling$hasHeatSource(Level level);
}
