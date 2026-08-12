package cn.breezeth.kaleidoscope_grilling;

import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackBlockEntity;


import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/** Server-side transactions for temporarily borrowing an item from an advanced rack. */
public final class AdvancedRackAutomationApi {
  public record BorrowReceipt(ResourceLocation dimension, BlockPos pos, int slot) {}

  public record BorrowResult(ItemStack stack, BorrowReceipt receipt) {
    public boolean success() {
      return !stack.isEmpty() && receipt != null;
    }
  }

  public record ReturnResult(boolean success, ItemStack remainder) {}

  public static BorrowResult borrow(
      ServerLevel level, BlockPos pos, Predicate<ItemStack> predicate, boolean simulate) {
    if (!(level.getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack))
      return new BorrowResult(ItemStack.EMPTY, null);
    for (int slot = 0; slot < rack.getContainerSize(); slot++) {
      ItemStack stored = rack.getItem(slot);
      if (stored.isEmpty() || !predicate.test(stored)) continue;
      ItemStack borrowed = stored.copyWithCount(1);
      if (!simulate) {
        rack.removeItem(slot, 1);
        rack.playPickupSound();
      }
      return new BorrowResult(
          borrowed, new BorrowReceipt(level.dimension().location(), pos.immutable(), slot));
    }
    return new BorrowResult(ItemStack.EMPTY, null);
  }

  public static ReturnResult returnBorrowed(
      ServerLevel level, BorrowReceipt receipt, ItemStack returned, boolean simulate) {
    if (receipt == null || returned.isEmpty() || !level.dimension().location().equals(receipt.dimension()))
      return new ReturnResult(false, returned.copy());
    if (!(level.getBlockEntity(receipt.pos()) instanceof AdvancedRackBlockEntity rack))
      return new ReturnResult(false, returned.copy());
    int slot = receipt.slot();
    if (slot < 0 || slot >= rack.getContainerSize() || !rack.canPlaceItem(slot, returned))
      return new ReturnResult(false, returned.copy());
    ItemStack stored = rack.getItem(slot);
    if (!stored.isEmpty()
        && (!ItemStack.isSameItemSameComponents(stored, returned)
            || stored.getCount() + returned.getCount() > stored.getMaxStackSize()))
      return new ReturnResult(false, returned.copy());
    if (!simulate) {
      if (stored.isEmpty()) rack.setItem(slot, returned.copy());
      else {
        stored.grow(returned.getCount());
        rack.setChanged();
        level.sendBlockUpdated(receipt.pos(), rack.getBlockState(), rack.getBlockState(), 3);
      }
      rack.playPlaceSound();
    }
    return new ReturnResult(true, ItemStack.EMPTY);
  }

  private AdvancedRackAutomationApi() {}
}
