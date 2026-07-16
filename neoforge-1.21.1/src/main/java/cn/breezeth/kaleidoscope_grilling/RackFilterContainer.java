package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class RackFilterContainer implements Container {
    private final AdvancedRackBlockEntity rack;

    RackFilterContainer(AdvancedRackBlockEntity rack) { this.rack = rack; }
    @Override public int getContainerSize() { return AdvancedRackBlockEntity.COMPARTMENT_COUNT; }
    @Override public boolean isEmpty() { for (int i = 0; i < getContainerSize(); i++) if (!rack.getFilter(i).isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return rack.getFilter(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    @Override public void setItem(int slot, ItemStack stack) {}
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return rack.stillValid(player); }
    @Override public void clearContent() { for (int i = 0; i < getContainerSize(); i++) rack.clearFilter(i); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
}
