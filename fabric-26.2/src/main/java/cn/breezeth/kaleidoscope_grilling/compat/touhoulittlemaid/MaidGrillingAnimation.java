package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

final class MaidGrillingAnimation {
  static ItemStack displayItem(Mob mob) {
    if (!(mob instanceof EntityMaid maid)
        || MaidGrillingData.KEY == null
        || maid.getTask() == null
        || !maid.getTask().getUid().equals(MaidGrillingTask.UID)) return ItemStack.EMPTY;
    MaidGrillingData data = maid.getOrCreateData(MaidGrillingData.KEY, MaidGrillingData.DEFAULT);
    if (data.action() == MaidGrillingData.Action.NONE
        || data.actionUntil() <= maid.level().getGameTime()) return ItemStack.EMPTY;
    return data.displayItem();
  }

  private MaidGrillingAnimation() {}
}
