package cn.breezeth.kaleidoscope_grilling.rack;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModMenus;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class RackSelectionMenu extends AbstractContainerMenu {
  public static final int DEPOSIT_ALL_BUTTON = 9;
  private final List<BlockPos> positions;
  private final ContainerData state;

  public RackSelectionMenu(int id, Inventory inv) {
    this(id, inv, List.of(), new SimpleContainerData(1));
  }

  public RackSelectionMenu(int id, Inventory inv, List<BlockPos> positions) {
    this(
        id,
        inv,
        List.copyOf(positions),
        new ContainerData() {
          public int get(int i) {
            return positions.size();
          }

          public void set(int i, int v) {}

          public int getCount() {
            return 1;
          }
        });
  }

  private RackSelectionMenu(int id, Inventory inv, List<BlockPos> positions, ContainerData state) {
    super(ModMenus.RACK_SELECTION.get(), id);
    this.positions = positions;
    this.state = state;
    SimpleContainer icons = new SimpleContainer(9);
    if (!positions.isEmpty())
      for (int i = 0; i < positions.size() && i < 9; i++)
        icons.setItem(i, ModBlocks.ADVANCED_RACK_ITEM.get().getDefaultInstance());
    addDataSlots(state);
    for (int i = 0; i < 5; i++) addSlot(readOnly(icons, i, 43 + i * 18, 25));
    for (int i = 0; i < 4; i++) addSlot(readOnly(icons, 5 + i, 52 + i * 18, 49));
  }

  private static Slot readOnly(SimpleContainer c, int s, int x, int y) {
    return new Slot(c, s, x, y) {
      public boolean mayPlace(ItemStack stack) {
        return false;
      }

      public boolean mayPickup(Player p) {
        return false;
      }
    };
  }

  public int rackCount() {
    return state.get(0);
  }

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (!(player instanceof ServerPlayer sp)) return false;
    if (id >= 0 && id < positions.size()) {
      AdvancedRackBlockEntity rack = valid(sp, positions.get(id));
      if (rack != null) sp.openMenu(rack.shortcutMenu());
      return true;
    }
    if (id == DEPOSIT_ALL_BUTTON) {
      boolean moved = false;
      for (BlockPos pos : positions) {
        AdvancedRackBlockEntity rack = valid(sp, pos);
        if (rack != null) moved |= rack.depositMatching(sp);
      }
      if (!moved)
        sp.displayClientMessage(
            Component.translatable("message.kaleidoscope_grilling.rack_nothing_to_deposit"), true);
      return true;
    }
    return false;
  }

  private static AdvancedRackBlockEntity valid(ServerPlayer p, BlockPos pos) {
    if (!p.level().hasChunkAt(pos)
        || p.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64) return null;
    return p.level().getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack ? rack : null;
  }

  public ItemStack quickMoveStack(Player p, int i) {
    return ItemStack.EMPTY;
  }

  public boolean stillValid(Player p) {
    if (!(p instanceof ServerPlayer sp)) return rackCount() > 0;
    for (BlockPos pos : positions) if (valid(sp, pos) != null) return true;
    return false;
  }

  public static MenuProvider provider(List<BlockPos> positions) {
    return new MenuProvider() {
      public Component getDisplayName() {
        return Component.translatable("container.kaleidoscope_grilling.rack_selection");
      }

      public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new RackSelectionMenu(id, inv, positions);
      }
    };
  }
}
