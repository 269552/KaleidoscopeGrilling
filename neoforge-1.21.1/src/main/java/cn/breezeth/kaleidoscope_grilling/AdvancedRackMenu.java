package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AdvancedRackMenu extends AbstractContainerMenu {
  public static final int DEPOSIT_BUTTON = 9;
  private static final int FILTER_START = 9;
  private static final int PLAYER_START = 18;
  private final Container rack;
  private final AdvancedRackBlockEntity blockEntity;
  private final ContainerData state;

  public AdvancedRackMenu(int id, Inventory inventory) {
    this(
        id,
        inventory,
        new SimpleContainer(AdvancedRackBlockEntity.COMPARTMENT_COUNT),
        new SimpleContainer(AdvancedRackBlockEntity.COMPARTMENT_COUNT),
        null,
        new SimpleContainerData(2));
  }

  public AdvancedRackMenu(
      int id, Inventory inventory, AdvancedRackBlockEntity rack, boolean shortcut) {
    this(
        id,
        inventory,
        rack,
        rack.filterContainer(),
        rack,
        new ContainerData() {
          @Override
          public int get(int index) {
            if (index == 0 && inventory.player instanceof ServerPlayer serverPlayer)
              return rack.rememberedSlot(serverPlayer);
            return index == 1 && shortcut ? 1 : 0;
          }

          @Override
          public void set(int index, int value) {}

          @Override
          public int getCount() {
            return 2;
          }
        });
  }

  private AdvancedRackMenu(
      int id,
      Inventory inventory,
      Container rack,
      Container filters,
      AdvancedRackBlockEntity blockEntity,
      ContainerData state) {
    super(ModMenus.ADVANCED_RACK.get(), id);
    this.rack = rack;
    this.blockEntity = blockEntity;
    this.state = state;
    rack.startOpen(inventory.player);
    addDataSlots(state);

    int[] topSlots = {26, 59, 90, 121, 153};
    int[] bottomSlots = {24, 60, 108, 144};
    for (int i = 0; i < topSlots.length; i++) addSlot(rackSlot(rack, i, topSlots[i], 29));
    for (int i = 0; i < bottomSlots.length; i++)
      addSlot(rackSlot(rack, 5 + i, bottomSlots[i], 110));
    for (int i = 0; i < AdvancedRackBlockEntity.COMPARTMENT_COUNT; i++) {
      addSlot(
          new Slot(filters, i, -10000, -10000) {
            @Override
            public boolean mayPlace(ItemStack stack) {
              return false;
            }

            @Override
            public boolean mayPickup(Player player) {
              return false;
            }
          });
    }
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++)
        addSlot(new Slot(inventory, col + row * 9 + 9, 26 + col * 18, 153 + row * 18));
    }
    for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 26 + col * 18, 210));
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

  public ItemStack getFilter(int compartment) {
    return slots.get(FILTER_START + compartment).getItem();
  }

  @Override
  public void clicked(int slotId, int button, ClickType clickType, Player player) {
    if (slotId >= 0
        && slotId < AdvancedRackBlockEntity.COMPARTMENT_COUNT
        && clickType == ClickType.PICKUP
        && getCarried().isEmpty()
        && slots.get(slotId).getItem().isEmpty()
        && !getFilter(slotId).isEmpty()) {
      if (blockEntity != null && !player.level().isClientSide) blockEntity.clearFilter(slotId);
      return;
    }
    int before = blockEntity != null && !player.level().isClientSide ? rackItemCount() : -1;
    super.clicked(slotId, button, clickType, player);
    if (before >= 0) {
      int after = rackItemCount();
      if (after > before) blockEntity.playPlaceSound();
      else if (after < before) blockEntity.playPickupSound();
    }
  }

  private int rackItemCount() {
    int count = 0;
    for (int i = 0; i < AdvancedRackBlockEntity.COMPARTMENT_COUNT; i++)
      count += slots.get(i).getItem().getCount();
    return count;
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!(player instanceof ServerPlayer serverPlayer) || blockEntity == null) return false;
    if (!stillValid(player)) return false;
    if (id >= 0 && id < AdvancedRackBlockEntity.COMPARTMENT_COUNT) {
      boolean success = blockEntity.swapWithHotbar(serverPlayer, id);
      if (!success)
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "message.kaleidoscope_grilling.rack_swap_failed"),
            true);
      return true;
    }
    if (id == DEPOSIT_BUTTON) {
      if (!blockEntity.depositMatching(serverPlayer))
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "message.kaleidoscope_grilling.rack_nothing_to_deposit"),
            true);
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
    if (index < AdvancedRackBlockEntity.COMPARTMENT_COUNT) {
      if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
    } else if (index >= PLAYER_START
        && !moveItemStackTo(stack, 0, AdvancedRackBlockEntity.COMPARTMENT_COUNT, false)) {
      return ItemStack.EMPTY;
    } else if (index < PLAYER_START) {
      return ItemStack.EMPTY;
    }
    if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
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
