package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import net.minecraft.world.item.ItemStack;

public final class GrillingWirelessIOData {
  private static final String GRILLING_MODE = "KaleidoscopeGrillingMode";

  public static boolean isEnabled(ItemStack stack) {
    return stack.hasTag() && stack.getTag().getBoolean(GRILLING_MODE);
  }

  public static void setEnabled(ItemStack stack, boolean enabled) {
    if (enabled) stack.getOrCreateTag().putBoolean(GRILLING_MODE, true);
    else if (stack.hasTag()) {
      stack.getTag().remove(GRILLING_MODE);
      if (stack.getTag().isEmpty()) stack.setTag(null);
    }
  }

  private GrillingWirelessIOData() {}
}
