package cn.breezeth.kaleidoscope_grilling.rack;

import cn.breezeth.kaleidoscope_grilling.registry.ModMenus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class AdvancedRackMenu extends AbstractContainerMenu {
  public static final int DEPOSIT_BUTTON = 9;
  private static final int FILTER_START = 9, PLAYER_START = 18;
  private final Container rack;
  private final AdvancedRackBlockEntity blockEntity;
  private final ContainerData state;

  public AdvancedRackMenu(int id, Inventory inv) {
    this(id, inv, new SimpleContainer(9), new SimpleContainer(9), null, new SimpleContainerData(2));
  }

  public AdvancedRackMenu(int id, Inventory inv, AdvancedRackBlockEntity rack, boolean shortcut) {
    this(
        id,
        inv,
        rack,
        rack.filterContainer(),
        rack,
        new ContainerData() {
          public int get(int index) {
            if (index == 0 && inv.player instanceof ServerPlayer sp) return rack.rememberedSlot(sp);
            return index == 1 && shortcut ? 1 : 0;
          }

          public void set(int i, int v) {}

          public int getCount() {
            return 2;
          }
        });
  }

  private AdvancedRackMenu(
      int id,
      Inventory inv,
      Container rack,
      Container filters,
      AdvancedRackBlockEntity be,
      ContainerData state) {
    super(ModMenus.ADVANCED_RACK.get(), id);
    this.rack = rack;
    this.blockEntity = be;
    this.state = state;
    rack.startOpen(inv.player);
    addDataSlots(state);
    int[] top = {26, 59, 90, 121, 153}, bottom = {24, 60, 108, 144};
    for (int i = 0; i < top.length; i++) addSlot(rackSlot(rack, i, top[i], 29));
    for (int i = 0; i < bottom.length; i++) addSlot(rackSlot(rack, 5 + i, bottom[i], 110));
    for (int i = 0; i < 9; i++)
      addSlot(
          new Slot(filters, i, -10000, -10000) {
            public boolean mayPlace(ItemStack s) {
              return false;
            }

            public boolean mayPickup(Player p) {
              return false;
            }
          });
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 9; col++)
        addSlot(new Slot(inv, col + row * 9 + 9, 26 + col * 18, 153 + row * 18));
    for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 26 + col * 18, 210));
  }

  private static Slot rackSlot(Container rack, int slot, int x, int y) {
    return new Slot(rack, slot, x, y) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return rack.canPlaceItem(slot, stack);
      }
    };
  }

  public boolean isShortcutMode() {
    return state.get(1) != 0;
  }

  public int rememberedSlot() {
    return state.get(0);
  }

  public ItemStack getFilter(int slot) {
    return slots.get(FILTER_START + slot).getItem();
  }

  @Override
  public void clicked(int slot, int button, ClickType type, Player player) {
    if (slot >= 0
        && slot < 9
        && type == ClickType.PICKUP
        && getCarried().isEmpty()
        && slots.get(slot).getItem().isEmpty()
        && !getFilter(slot).isEmpty()) {
      if (blockEntity != null && !player.level().isClientSide) blockEntity.clearFilter(slot);
      return;
    }
    int before = blockEntity != null && !player.level().isClientSide ? rackItemCount() : -1;
    super.clicked(slot, button, type, player);
    if (before >= 0) {
      int after = rackItemCount();
      if (after > before) blockEntity.playPlaceSound();
      else if (after < before) blockEntity.playPickupSound();
    }
  }

  private int rackItemCount() {
    int count = 0;
    for (int i = 0; i < 9; i++) count += slots.get(i).getItem().getCount();
    return count;
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!(player instanceof ServerPlayer sp) || blockEntity == null || !stillValid(player))
      return false;
    if (id >= 0 && id < 9) {
      if (!blockEntity.swapWithHotbar(sp, id))
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_swap_failed"), true);
      return true;
    }
    if (id == DEPOSIT_BUTTON) {
      if (!blockEntity.depositMatching(sp))
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_nothing_to_deposit"), true);
      return true;
    }
    return false;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack result = ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (!slot.hasItem()) return result;
    ItemStack stack = slot.getItem();
    result = stack.copy();
    if (index < 9) {
      if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
    } else if (index >= PLAYER_START) {
      if (!moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
    } else return ItemStack.EMPTY;
    if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
    else slot.setChanged();
    return result;
  }

  @Override
  public boolean stillValid(Player player) {
    return rack.stillValid(player);
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    rack.stopOpen(player);
  }
}
