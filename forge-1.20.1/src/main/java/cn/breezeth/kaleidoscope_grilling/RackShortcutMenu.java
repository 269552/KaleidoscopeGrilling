package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class RackShortcutMenu extends AbstractContainerMenu {
  public static final int DEPOSIT_BUTTON = 9;
  private final Container rack;
  private final AdvancedRackBlockEntity be;
  private final ContainerData state;

  public RackShortcutMenu(int id, Inventory inv) {
    this(id, inv, new SimpleContainer(9), new SimpleContainer(9), null, new SimpleContainerData(1));
  }

  public RackShortcutMenu(int id, Inventory inv, AdvancedRackBlockEntity rack) {
    this(
        id,
        inv,
        rack,
        rack.filterContainer(),
        rack,
        new ContainerData() {
          public int get(int i) {
            return inv.player instanceof ServerPlayer p ? rack.rememberedSlot(p) : -1;
          }

          public void set(int i, int v) {}

          public int getCount() {
            return 1;
          }
        });
  }

  private RackShortcutMenu(
      int id,
      Inventory inv,
      Container rack,
      Container filters,
      AdvancedRackBlockEntity be,
      ContainerData state) {
    super(ModMenus.RACK_SHORTCUT.get(), id);
    this.rack = rack;
    this.be = be;
    this.state = state;
    rack.startOpen(inv.player);
    addDataSlots(state);
    int[] top = {26, 59, 90, 121, 153}, bottom = {24, 60, 108, 144};
    for (int i = 0; i < top.length; i++) addSlot(ro(rack, i, top[i], 29));
    for (int i = 0; i < bottom.length; i++) addSlot(ro(rack, 5 + i, bottom[i], 108));
    for (int i = 0; i < 9; i++) addSlot(ro(filters, i, -10000, -10000));
  }

  private static Slot ro(Container c, int s, int x, int y) {
    return new Slot(c, s, x, y) {
      public boolean mayPlace(ItemStack stack) {
        return false;
      }

      public boolean mayPickup(Player p) {
        return false;
      }
    };
  }

  public ItemStack getFilter(int s) {
    return slots.get(9 + s).getItem();
  }

  public int rememberedSlot() {
    return state.get(0);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!(player instanceof ServerPlayer sp) || be == null || !stillValid(player)) return false;
    if (id >= 0 && id < 9) {
      if (!be.swapWithHotbar(sp, id))
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_swap_failed"), true);
      return true;
    }
    if (id == DEPOSIT_BUTTON) {
      if (!be.depositMatching(sp))
        player.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_nothing_to_deposit"), true);
      return true;
    }
    return false;
  }

  public ItemStack quickMoveStack(Player p, int i) {
    return ItemStack.EMPTY;
  }

  public boolean stillValid(Player p) {
    return rack.stillValid(p);
  }

  @Override
  public void removed(Player p) {
    super.removed(p);
    rack.stopOpen(p);
  }
}
