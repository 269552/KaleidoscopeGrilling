package cn.breezeth.kaleidoscope_grilling.compat.ordertocook;

import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class RefrigeratorSkewerSorter {
  public static final int NORMAL_SORT_ID = 0;
  public static final int FULL_SORT_ID = 1;
  private static final int MAX_STACK_SIZE = 128;
  private static final int PLAYER_SLOT_COUNT = 36;
  private static final long NORMAL_HEAT_WINDOW = 5L * 60L * 20L;

  public static void compact(Container inventory, int slotCount, Level level, boolean fullSort) {
    List<Group> groups = new ArrayList<>();
    for (int slot = 0; slot < slotCount; slot++) {
      ItemStack source = inventory.getItem(slot);
      if (source.isEmpty()) continue;
      Group match = null;
      for (Group group : groups) {
        if (canGroup(group.representative, source, level, fullSort)) {
          match = group;
          break;
        }
      }
      if (match == null) {
        match = new Group(source.copyWithCount(1));
        groups.add(match);
      }
      match.add(source, level);
    }

    List<ItemStack> compacted = new ArrayList<>();
    for (Group group : groups) {
      ItemStack representative = group.representative.copy();
      long averagedHeat = group.totalRemainingHeat / group.totalCount;
      if (averagedHeat > 0L) FoodState.setHot(representative, level.getGameTime() + averagedHeat);
      else FoodState.clearExpired(representative, level);
      int remaining = group.totalCount;
      while (remaining > 0) {
        int count = Math.min(remaining, MAX_STACK_SIZE);
        compacted.add(representative.copyWithCount(count));
        remaining -= count;
      }
    }

    for (int slot = 0; slot < slotCount; slot++) {
      inventory.setItem(slot, slot < compacted.size() ? compacted.get(slot) : ItemStack.EMPTY);
    }
    inventory.setChanged();
  }

  public static boolean hasFullSortCandidate(List<Slot> slots, Level level) {
    int containerSlots = Math.max(0, slots.size() - PLAYER_SLOT_COUNT);
    for (int first = 0; first < containerSlots; first++) {
      ItemStack a = slots.get(first).getItem();
      if (!isSkewer(a)) continue;
      for (int second = first + 1; second < containerSlots; second++) {
        ItemStack b = slots.get(second).getItem();
        if (isSkewer(b) && FoodState.sameForHeatMerge(a, b)) return true;
      }
    }
    return false;
  }

  private static boolean canGroup(ItemStack first, ItemStack second, Level level, boolean fullSort) {
    if (!isSkewer(first) || !isSkewer(second)) {
      return ItemStack.isSameItemSameComponents(first, second);
    }
    if (!FoodState.sameForHeatMerge(first, second)) return false;
    if (fullSort) return true;
    if (ItemStack.isSameItemSameComponents(first, second)) return true;
    if (!FoodState.isHot(first, level) || !FoodState.isHot(second, level)) return false;
    return Math.abs(FoodState.remainingHeat(first, level) - FoodState.remainingHeat(second, level))
        <= NORMAL_HEAT_WINDOW;
  }

  private static boolean isSkewer(ItemStack stack) {
    if (stack.isEmpty()) return false;
    var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
    return "kaleidoscope_grilling".equals(id.getNamespace()) && id.getPath().contains("skewer");
  }

  private static final class Group {
    private final ItemStack representative;
    private int totalCount;
    private long totalRemainingHeat;

    private Group(ItemStack representative) {
      this.representative = representative;
    }

    private void add(ItemStack stack, Level level) {
      totalCount += stack.getCount();
      totalRemainingHeat += FoodState.remainingHeat(stack, level) * stack.getCount();
    }
  }

  private RefrigeratorSkewerSorter() {}
}
