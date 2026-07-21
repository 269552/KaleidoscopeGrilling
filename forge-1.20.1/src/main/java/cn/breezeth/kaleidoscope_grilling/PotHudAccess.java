package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface PotHudAccess {
  int grilling$getStatus();

  List<ItemStack> grilling$getInputs();

  boolean grilling$hasHeatSource(Level level);
}
