package cn.breezeth.kaleidoscope_grilling.rack;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class RackFilterContainer implements Container {
  private final AdvancedRackBlockEntity rack;

  RackFilterContainer(AdvancedRackBlockEntity rack) {
    this.rack = rack;
  }

  public int getContainerSize() {
    return 9;
  }

  public boolean isEmpty() {
    for (int i = 0; i < 9; i++) if (!rack.getFilter(i).isEmpty()) return false;
    return true;
  }

  public ItemStack getItem(int slot) {
    return rack.getFilter(slot);
  }

  public ItemStack removeItem(int slot, int amount) {
    return ItemStack.EMPTY;
  }

  public ItemStack removeItemNoUpdate(int slot) {
    return ItemStack.EMPTY;
  }

  public void setItem(int slot, ItemStack stack) {}

  public void setChanged() {}

  public boolean stillValid(Player player) {
    return rack.stillValid(player);
  }

  public void clearContent() {
    for (int i = 0; i < 9; i++) rack.clearFilter(i);
  }

  public boolean canPlaceItem(int slot, ItemStack stack) {
    return false;
  }
}
