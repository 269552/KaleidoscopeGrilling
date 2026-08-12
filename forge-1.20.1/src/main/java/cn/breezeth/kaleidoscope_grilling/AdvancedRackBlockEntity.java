package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedRackBlockEntity extends BlockEntity implements Container, MenuProvider {
  public static final int COMPARTMENT_COUNT = 9;
  private static final String MEMORY_TAG = "KaleidoscopeGrillingRackMemory";
  private NonNullList<ItemStack> items = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);
  private NonNullList<ItemStack> filters = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);

  public AdvancedRackBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.ADVANCED_RACK.get(), pos, state);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("container.kaleidoscope_grilling.advanced_rack");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return new AdvancedRackMenu(id, inventory, this, false);
  }

  public MenuProvider shortcutMenu() {
    return new MenuProvider() {
      @Override
      public Component getDisplayName() {
        return Component.translatable("container.kaleidoscope_grilling.advanced_rack_shortcut");
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new RackShortcutMenu(id, inv, AdvancedRackBlockEntity.this);
      }
    };
  }

  @Override
  public int getContainerSize() {
    return COMPARTMENT_COUNT;
  }

  @Override
  public boolean isEmpty() {
    return items.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int slot) {
    return items.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack out = ContainerHelper.removeItem(items, slot, amount);
    if (!out.isEmpty()) sync();
    return out;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return ContainerHelper.takeItem(items, slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    if (!stack.isEmpty() && !canPlaceItem(slot, stack)) return;
    if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
    if (!stack.isEmpty() && filters.get(slot).isEmpty()) {
      ItemStack filter = stack.copy();
      filter.setCount(1);
      filters.set(slot, filter);
    }
    items.set(slot, stack);
    sync();
  }

  @Override
  public boolean stillValid(Player player) {
    return level != null
        && level.getBlockEntity(worldPosition) == this
        && player.distanceToSqr(
                worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5)
            <= 64;
  }

  @Override
  public void clearContent() {
    items.clear();
    sync();
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    if (!AdvancedRackCompatApi.canPlace(slot, stack)) return false;
    ItemStack filter = filters.get(slot);
    return filter.isEmpty() || canShareCategory(filter, stack);
  }

  public ItemStack getFilter(int slot) {
    return filters.get(slot);
  }

  public void clearFilter(int slot) {
    if (slot >= 0 && slot < COMPARTMENT_COUNT && getItem(slot).isEmpty()) {
      filters.set(slot, ItemStack.EMPTY);
      sync();
    }
  }

  public Container filterContainer() {
    return new RackFilterContainer(this);
  }

  public boolean depositMatching(ServerPlayer player) {
    boolean changed = false;
    Inventory inventory = player.getInventory();
    for (int hotbar = 0; hotbar < 9; hotbar++) {
      ItemStack carried = inventory.getItem(hotbar);
      int rackSlot = rememberedSlot(player, hotbar);
      if (carried.isEmpty() || rackSlot < 0) continue;
      ItemStack filter = filters.get(rackSlot);
      if (!filter.isEmpty() && !canShareCategory(filter, carried)) continue;
      ItemStack stored = getItem(rackSlot);
      int room =
          stored.isEmpty()
              ? carried.getMaxStackSize()
              : stored.getMaxStackSize() - stored.getCount();
      int move = Math.min(room, carried.getCount());
      if (move <= 0) continue;
      insert(rackSlot, stored, carried, move);
      clearMemory(player, hotbar);
      changed = true;
    }
    for (int i = 0; i < 36; i++) {
      ItemStack carried = inventory.getItem(i);
      if (carried.isEmpty() || (i < 9 && binding(player, i) != null)) continue;
      int rackSlot = findMatchingSlotWithRoom(carried);
      if (rackSlot < 0) continue;
      ItemStack stored = getItem(rackSlot);
      int room =
          stored.isEmpty()
              ? carried.getMaxStackSize()
              : stored.getMaxStackSize() - stored.getCount();
      int move = Math.min(room, carried.getCount());
      if (move > 0) {
        insert(rackSlot, stored, carried, move);
        changed = true;
      }
    }
    if (changed) {
      inventory.setChanged();
      sync();
      playPlaceSound();
    }
    return changed;
  }

  private void insert(int slot, ItemStack stored, ItemStack carried, int move) {
    if (stored.isEmpty()) {
      ItemStack moved = carried.copy();
      moved.setCount(move);
      setItem(slot, moved);
    } else stored.grow(move);
    carried.shrink(move);
  }

  public boolean swapWithHotbar(ServerPlayer player, int compartment) {
    if (compartment < 0 || compartment >= COMPARTMENT_COUNT || getItem(compartment).isEmpty())
      return false;
    Inventory inventory = player.getInventory();
    int hotbar = inventory.selected;
    ItemStack held = inventory.getItem(hotbar), requested = getItem(compartment);
    if (ItemStack.isSameItemSameTags(held, requested)) {
      int move = Math.min(requested.getCount(), held.getMaxStackSize() - held.getCount());
      if (move > 0) {
        held.grow(move);
        requested.shrink(move);
        playPickupSound();
      }
      remember(player, hotbar, compartment);
      sync();
      return true;
    }
    AdvancedRackBlockEntity returnRack = null;
    int destination = -1;
    RackBinding previous = binding(player, hotbar);
    if (!held.isEmpty() && previous != null) {
      AdvancedRackBlockEntity old = findBoundRack(player, previous);
      if (old != null
          && (old != this || previous.slot() != compartment)
          && old.canReceiveReturned(previous.slot(), held)) {
        returnRack = old;
        destination = previous.slot();
      }
    }
    if (!held.isEmpty() && returnRack == null) {
      int local = findReturnSlot(held, compartment);
      if (local >= 0) {
        returnRack = this;
        destination = local;
      }
    }
    if (!held.isEmpty() && returnRack == null && !canFitInInventory(inventory, hotbar, held))
      return false;
    ItemStack replacement = requested.copy();
    setItem(compartment, ItemStack.EMPTY);
    if (!held.isEmpty()) {
      if (returnRack != null) returnRack.returnItem(destination, held);
      else {
        ItemStack toStore = held.copy();
        inventory.setItem(hotbar, ItemStack.EMPTY);
        storeInInventory(inventory, hotbar, toStore);
      }
    }
    inventory.setItem(hotbar, replacement);
    remember(player, hotbar, compartment);
    inventory.setChanged();
    sync();
    playPickupSound();
    return true;
  }

  public void playPickupSound() {
    if (level != null && !level.isClientSide)
      level.playSound(
          null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
  }

  public void playPlaceSound() {
    if (level != null && !level.isClientSide)
      level.playSound(
          null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
  }

  public int rememberedSlot(ServerPlayer player) {
    return rememberedSlot(player, player.getInventory().selected);
  }

  private int rememberedSlot(ServerPlayer player, int hotbar) {
    RackBinding binding = binding(player, hotbar);
    return binding != null && binding.pos().equals(worldPosition) ? binding.slot() : -1;
  }

  private void remember(ServerPlayer player, int hotbar, int slot) {
    CompoundTag root = player.getPersistentData(), tag = root.getCompound(MEMORY_TAG);
    tag.putLong("Pos" + hotbar, worldPosition.asLong());
    tag.putString("Dim" + hotbar, player.level().dimension().location().toString());
    tag.putInt("Slot" + hotbar, slot);
    root.put(MEMORY_TAG, tag);
  }

  public void clearMemory(ServerPlayer player, int hotbar) {
    CompoundTag root = player.getPersistentData(), tag = root.getCompound(MEMORY_TAG);
    tag.remove("Pos" + hotbar);
    tag.remove("Dim" + hotbar);
    tag.remove("Slot" + hotbar);
    root.put(MEMORY_TAG, tag);
  }

  public static BlockPos selectedBindingPos(ServerPlayer player) {
    int hotbar = player.getInventory().selected;
    CompoundTag tag = player.getPersistentData().getCompound(MEMORY_TAG);
    if (!tag.contains("Pos" + hotbar)
        || !tag.getString("Dim" + hotbar).equals(player.level().dimension().location().toString()))
      return null;
    return BlockPos.of(tag.getLong("Pos" + hotbar));
  }

  public static void clearSelectedMemory(ServerPlayer player) {
    int hotbar = player.getInventory().selected;
    CompoundTag root = player.getPersistentData(), tag = root.getCompound(MEMORY_TAG);
    tag.remove("Pos" + hotbar);
    tag.remove("Dim" + hotbar);
    tag.remove("Slot" + hotbar);
    root.put(MEMORY_TAG, tag);
  }

  private RackBinding binding(ServerPlayer player, int hotbar) {
    CompoundTag tag = player.getPersistentData().getCompound(MEMORY_TAG);
    String dim = player.level().dimension().location().toString();
    if (!tag.contains("Pos" + hotbar) || !tag.getString("Dim" + hotbar).equals(dim)) return null;
    int slot = tag.getInt("Slot" + hotbar);
    return slot >= 0 && slot < COMPARTMENT_COUNT
        ? new RackBinding(BlockPos.of(tag.getLong("Pos" + hotbar)), slot)
        : null;
  }

  private AdvancedRackBlockEntity findBoundRack(ServerPlayer player, RackBinding binding) {
    BlockPos pos = binding.pos();
    if (!player.level().hasChunkAt(pos)
        || player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64)
      return null;
    return player.level().getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack ? rack : null;
  }

  private boolean canReceiveReturned(int slot, ItemStack stack) {
    if (slot < 0 || slot >= COMPARTMENT_COUNT || !canPlaceItem(slot, stack)) return false;
    ItemStack stored = getItem(slot);
    return stored.isEmpty()
        || ItemStack.isSameItemSameTags(stored, stack)
            && stored.getCount() + stack.getCount() <= stored.getMaxStackSize();
  }

  private void returnItem(int slot, ItemStack stack) {
    ItemStack stored = getItem(slot);
    if (stored.isEmpty()) setItem(slot, stack);
    else {
      stored.grow(stack.getCount());
      sync();
    }
  }

  private static boolean canFitInInventory(Inventory inventory, int excluded, ItemStack stack) {
    int remaining = stack.getCount();
    for (int slot = 0; slot < 36 && remaining > 0; slot++) {
      if (slot == excluded) continue;
      ItemStack stored = inventory.getItem(slot);
      if (stored.isEmpty()) remaining -= stack.getMaxStackSize();
      else if (ItemStack.isSameItemSameTags(stored, stack))
        remaining -= stored.getMaxStackSize() - stored.getCount();
    }
    return remaining <= 0;
  }

  private static void storeInInventory(Inventory inventory, int excluded, ItemStack stack) {
    for (int slot = 0; slot < 36 && !stack.isEmpty(); slot++) {
      if (slot == excluded) continue;
      ItemStack stored = inventory.getItem(slot);
      if (!stored.isEmpty() && ItemStack.isSameItemSameTags(stored, stack)) {
        int move = Math.min(stack.getCount(), stored.getMaxStackSize() - stored.getCount());
        stored.grow(move);
        stack.shrink(move);
      }
    }
    for (int slot = 0; slot < 36 && !stack.isEmpty(); slot++) {
      if (slot == excluded || !inventory.getItem(slot).isEmpty()) continue;
      int move = Math.min(stack.getCount(), stack.getMaxStackSize());
      ItemStack moved = stack.copy();
      moved.setCount(move);
      inventory.setItem(slot, moved);
      stack.shrink(move);
    }
  }

  private int findMatchingSlotWithRoom(ItemStack stack) {
    for (int i = 0; i < COMPARTMENT_COUNT; i++) {
      ItemStack stored = getItem(i), filter = filters.get(i);
      int room =
          stored.isEmpty() ? stack.getMaxStackSize() : stored.getMaxStackSize() - stored.getCount();
      if (!filter.isEmpty()
          && canPlaceItem(i, stack)
          && canShareCategory(filter, stack)
          && room > 0) return i;
    }
    return -1;
  }

  private int findReturnSlot(ItemStack stack, int excluded) {
    for (int slot = 0; slot < COMPARTMENT_COUNT; slot++)
      if (slot != excluded && !filters.get(slot).isEmpty() && canReceiveReturned(slot, stack))
        return slot;
    return -1;
  }

  private void sync() {
    setChanged();
    updateDisplayState();
    if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  private void updateDisplayState() {
    if (level == null || level.isClientSide) return;
    int occupied = 0;
    for (int slot = 0; slot < 5; slot++) if (!items.get(slot).isEmpty()) occupied++;
    int display = Math.min(4, occupied);
    BlockState state = getBlockState();
    if (state.hasProperty(AdvancedRackBlock.SPICE_LEVEL)
        && state.getValue(AdvancedRackBlock.SPICE_LEVEL) != display)
      level.setBlock(worldPosition, state.setValue(AdvancedRackBlock.SPICE_LEVEL, display), 3);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    updateDisplayState();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    ContainerHelper.saveAllItems(tag, items);
    CompoundTag filterTag = new CompoundTag();
    ContainerHelper.saveAllItems(filterTag, filters);
    tag.put("Filters", filterTag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    items = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);
    ContainerHelper.loadAllItems(tag, items);
    filters = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);
    if (tag.contains("Filters")) ContainerHelper.loadAllItems(tag.getCompound("Filters"), filters);
    for (int i = 0; i < COMPARTMENT_COUNT; i++)
      if (filters.get(i).isEmpty() && !items.get(i).isEmpty()) {
        ItemStack filter = items.get(i).copy();
        filter.setCount(1);
        filters.set(i, filter);
      }
  }

  @Override
  public CompoundTag getUpdateTag() {
    return saveWithoutMetadata();
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  public void restoreFromItem(CompoundTag tag) {
    load(tag.copy());
    sync();
  }

  private static boolean canShareCategory(ItemStack a, ItemStack b) {
    if (isSeasoningBottle(a) && isSeasoningBottle(b)) return true;
    if (!a.isStackable() && !b.isStackable() && a.isDamageableItem() && b.isDamageableItem())
      return a.getItem() == b.getItem();
    if (OilPotCompat.isOilPot(a) && OilPotCompat.isOilPot(b))
      return a.getItem() == b.getItem() && OilPotCompat.getType(a).equals(OilPotCompat.getType(b));
    return ItemStack.isSameItemSameTags(a, b);
  }

  private static boolean isSeasoningBottle(ItemStack stack) {
    return stack.is(ModItems.EMPTY_SEASONING_BOTTLE.get())
        || stack.is(ModItems.PENDING_SEASONING.get())
        || stack.is(ModItems.SPECIAL_SEASONING.get());
  }

  private record RackBinding(BlockPos pos, int slot) {}
}
