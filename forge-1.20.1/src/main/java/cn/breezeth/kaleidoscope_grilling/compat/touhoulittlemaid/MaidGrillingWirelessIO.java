package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

final class MaidGrillingWirelessIO {
  static List<MaidGrillingData.BoundContainer> supplyEndpoints(EntityMaid maid) {
    return endpoints(maid, false);
  }

  static List<MaidGrillingData.BoundContainer> outputEndpoints(EntityMaid maid) {
    return endpoints(maid, true);
  }

  static boolean hasSupplyEndpoint(EntityMaid maid) {
    return hasEndpoint(maid, false);
  }

  static boolean hasOutputEndpoint(EntityMaid maid) {
    return hasEndpoint(maid, true);
  }

  private static List<MaidGrillingData.BoundContainer> endpoints(
      EntityMaid maid, boolean maidToChest) {
    List<MaidGrillingData.BoundContainer> result = new ArrayList<>();
    for (int slot = 0; slot < maid.getMaidBauble().getSlots(); slot++) {
      ItemStack stack = maid.getMaidBauble().getStackInSlot(slot);
      if (!isEndpoint(stack, maidToChest)) continue;
      BlockPos pos = ItemWirelessIO.getBindingPos(stack);
      if (pos != null) {
        result.add(
            new MaidGrillingData.BoundContainer(
                maid.level().dimension().location(), pos.immutable()));
      }
    }
    return result;
  }

  private static boolean hasEndpoint(EntityMaid maid, boolean maidToChest) {
    for (int slot = 0; slot < maid.getMaidBauble().getSlots(); slot++) {
      ItemStack stack = maid.getMaidBauble().getStackInSlot(slot);
      if (isEndpoint(stack, maidToChest) && ItemWirelessIO.getBindingPos(stack) != null) return true;
    }
    return false;
  }

  private static boolean isEndpoint(ItemStack stack, boolean maidToChest) {
    return stack.is(InitItems.WIRELESS_IO.get())
        && GrillingWirelessIOData.isEnabled(stack)
        && ItemWirelessIO.isMaidToChest(stack) == maidToChest;
  }

  private MaidGrillingWirelessIO() {}
}
