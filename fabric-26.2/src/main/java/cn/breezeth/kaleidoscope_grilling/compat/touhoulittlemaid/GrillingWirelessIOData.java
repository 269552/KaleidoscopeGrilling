package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class GrillingWirelessIOData {
  private static final String GRILLING_MODE = "KaleidoscopeGrillingMode";

  public static boolean isEnabled(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean(GRILLING_MODE);
  }

  public static void setEnabled(ItemStack stack, boolean enabled) {
    CompoundTag tag =
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    if (enabled) tag.putBoolean(GRILLING_MODE, true);
    else tag.remove(GRILLING_MODE);
    if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
    else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  private GrillingWirelessIOData() {}
}
