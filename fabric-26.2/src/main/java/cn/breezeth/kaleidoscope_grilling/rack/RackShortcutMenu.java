package cn.breezeth.kaleidoscope_grilling.rack;

import cn.breezeth.kaleidoscope_grilling.registry.ModMenus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class RackShortcutMenu extends AbstractContainerMenu {
  public static final int DEPOSIT_BUTTON = 9;
  private final Container rack;
  private final AdvancedRackBlockEntity blockEntity;
  private final ContainerData state;

  public RackShortcutMenu(int id, Inventory inventory) {
    this(
        id,
        inventory,
        new SimpleContainer(9),
        new SimpleContainer(9),
        null,
        new SimpleContainerData(1));
  }

  public RackShortcutMenu(int id, Inventory inventory, AdvancedRackBlockEntity rack) {
    this(
        id,
        inventory,
        rack,
        rack.filterContainer(),
        rack,
        new ContainerData() {
          @Override
          public int get(int index) {
            return inventory.player instanceof ServerPlayer player
                ? rack.rememberedSlot(player)
                : -1;
          }

          @Override
          public void set(int index, int value) {}

          @Override
          public int getCount() {
            return 1;
          }
        });
  }

  private RackShortcutMenu(
      int id,
      Inventory inventory,
      Container rack,
      Container filters,
      AdvancedRackBlockEntity blockEntity,
      ContainerData state) {
    super(ModMenus.RACK_SHORTCUT.get(), id);
    this.rack = rack;
    this.blockEntity = blockEntity;
    this.state = state;
    rack.startOpen(inventory.player);
    addDataSlots(state);
    int[] topSlots = {26, 59, 90, 121, 153};
    int[] bottomSlots = {24, 60, 108, 144};
    for (int i = 0; i < topSlots.length; i++) addSlot(readOnly(rack, i, topSlots[i], 29));
    for (int i = 0; i < bottomSlots.length; i++)
      addSlot(readOnly(rack, 5 + i, bottomSlots[i], 108));
    for (int i = 0; i < 9; i++) addSlot(readOnly(filters, i, -10000, -10000));
  }

  private static Slot readOnly(Container container, int slot, int x, int y) {
    return new Slot(container, slot, x, y) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return false;
      }

      @Override
      public boolean mayPickup(Player player) {
        return false;
      }
    };
  }

  public ItemStack getFilter(int slot) {
    return slots.get(9 + slot).getItem();
  }

  public int rememberedSlot() {
    return state.get(0);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!(player instanceof ServerPlayer serverPlayer)
        || blockEntity == null
        || !stillValid(player)) return false;
    if (id >= 0 && id < 9) {
      if (!blockEntity.swapWithHotbar(serverPlayer, id))
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_swap_failed"), true);
      return true;
    }
    if (id == DEPOSIT_BUTTON) {
      if (!blockEntity.depositMatching(serverPlayer))
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_nothing_to_deposit"), true);
      return true;
    }
    return false;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
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
