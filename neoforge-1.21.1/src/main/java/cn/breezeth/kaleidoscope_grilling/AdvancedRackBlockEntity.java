package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedRackBlockEntity extends BaseContainerBlockEntity {
  public static final int COMPARTMENT_COUNT = 9;
  private static final String MEMORY_TAG = "KaleidoscopeGrillingRackMemory";
  private NonNullList<ItemStack> items = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);
  private NonNullList<ItemStack> filters = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);

  public AdvancedRackBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.ADVANCED_RACK.get(), pos, state);
  }

  @Override
  protected Component getDefaultName() {
    return Component.translatable("container.kaleidoscope_grilling.advanced_rack");
  }

  @Override
  protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
    return new AdvancedRackMenu(id, inventory, this, false);
  }

  public MenuProvider shortcutMenu() {
    return new MenuProvider() {
      @Override
      public Component getDisplayName() {
        return Component.translatable("container.kaleidoscope_grilling.advanced_rack_shortcut");
      }

      @Override
      public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new RackShortcutMenu(id, inventory, AdvancedRackBlockEntity.this);
      }
    };
  }

  @Override
  public int getContainerSize() {
    return COMPARTMENT_COUNT;
  }

  @Override
  protected NonNullList<ItemStack> getItems() {
    return items;
  }

  @Override
  protected void setItems(NonNullList<ItemStack> stacks) {
    items = stacks;
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack removed = super.removeItem(slot, amount);
    if (!removed.isEmpty()) sync();
    return removed;
  }

  @Override
  public void clearContent() {
    super.clearContent();
    sync();
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    if (!AdvancedRackCompatApi.canPlace(slot, stack)) return false;
    ItemStack filter = filters.get(slot);
    return filter.isEmpty() || canShareCategory(filter, stack);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    if (!stack.isEmpty() && !canPlaceItem(slot, stack)) return;
    if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
    if (!stack.isEmpty() && filters.get(slot).isEmpty()) filters.set(slot, stack.copyWithCount(1));
    super.setItem(slot, stack);
    sync();
  }

  public ItemStack getFilter(int slot) {
    return filters.get(slot);
  }

  public List<ItemStack> copyStoredItems() {
    return items.stream().map(ItemStack::copy).toList();
  }

  public void clearFilter(int slot) {
    if (slot < 0 || slot >= COMPARTMENT_COUNT || !getItem(slot).isEmpty()) return;
    filters.set(slot, ItemStack.EMPTY);
    sync();
  }

  public Container filterContainer() {
    return new RackFilterContainer(this);
  }

  public boolean depositMatching(ServerPlayer player) {
    boolean changed = false;
    Inventory inventory = player.getInventory();
    for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
      ItemStack carried = inventory.getItem(hotbarSlot);
      int rackSlot = rememberedSlot(player, hotbarSlot);
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
      if (stored.isEmpty()) setItem(rackSlot, carried.copyWithCount(move));
      else stored.grow(move);
      carried.shrink(move);
      clearMemory(player, hotbarSlot);
      changed = true;
    }
    for (int playerSlot = 0; playerSlot < 36; playerSlot++) {
      ItemStack carried = inventory.getItem(playerSlot);
      if (carried.isEmpty()) continue;
      if (playerSlot < 9 && binding(player, playerSlot) != null) continue;
      int rackSlot = findMatchingSlotWithRoom(carried);
      if (rackSlot < 0) continue;
      ItemStack stored = getItem(rackSlot);
      int room =
          stored.isEmpty()
              ? carried.getMaxStackSize()
              : stored.getMaxStackSize() - stored.getCount();
      int move = Math.min(carried.getCount(), room);
      if (move <= 0) continue;
      if (stored.isEmpty()) setItem(rackSlot, carried.copyWithCount(move));
      else stored.grow(move);
      carried.shrink(move);
      changed = true;
    }
    if (changed) {
      inventory.setChanged();
      sync();
      playPlaceSound();
    }
    return changed;
  }

  public boolean swapWithHotbar(ServerPlayer player, int compartment) {
    if (compartment < 0 || compartment >= COMPARTMENT_COUNT) return false;
    ItemStack requested = getItem(compartment);
    if (requested.isEmpty()) return false;

    Inventory inventory = player.getInventory();
    int hotbarSlot = inventory.selected;
    ItemStack held = inventory.getItem(hotbarSlot);
    if (ItemStack.isSameItemSameComponents(held, requested)) {
      int move = Math.min(requested.getCount(), held.getMaxStackSize() - held.getCount());
      if (move > 0) {
        held.grow(move);
        requested.shrink(move);
        playPickupSound();
      }
      remember(player, hotbarSlot, compartment);
      sync();
      return true;
    }

    AdvancedRackBlockEntity returnRack = null;
    int returnSlot = -1;
    RackBinding previous = binding(player, hotbarSlot);
    if (!held.isEmpty() && previous != null) {
      AdvancedRackBlockEntity previousRack = findBoundRack(player, previous);
      if (previousRack != null
          && (previousRack != this || previous.slot() != compartment)
          && previousRack.canReceiveReturned(previous.slot(), held)) {
        returnRack = previousRack;
        returnSlot = previous.slot();
      }
    }

    if (!held.isEmpty() && returnRack == null) {
      int localReturnSlot = findReturnSlot(held, compartment);
      if (localReturnSlot >= 0) {
        returnRack = this;
        returnSlot = localReturnSlot;
      }
    }

    if (!held.isEmpty() && returnRack == null && !canFitInInventory(inventory, hotbarSlot, held))
      return false;

    ItemStack replacement = requested.copy();
    setItem(compartment, ItemStack.EMPTY);
    if (!held.isEmpty()) {
      if (returnRack != null) returnRack.returnItem(returnSlot, held);
      else {
        ItemStack toStore = held.copy();
        inventory.setItem(hotbarSlot, ItemStack.EMPTY);
        storeInInventory(inventory, hotbarSlot, toStore);
      }
    }
    inventory.setItem(hotbarSlot, replacement);
    remember(player, hotbarSlot, compartment);
    inventory.setChanged();
    sync();
    playPickupSound();
    return true;
  }

  public void playPickupSound() {
    if (level != null && !level.isClientSide) {
      level.playSound(
          null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
  }

  public void playPlaceSound() {
    if (level != null && !level.isClientSide) {
      level.playSound(
          null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
  }

  public int rememberedSlot(ServerPlayer player) {
    return rememberedSlot(player, player.getInventory().selected);
  }

  public void clearMemory(ServerPlayer player, int hotbarSlot) {
    CompoundTag root = player.getPersistentData();
    CompoundTag memory = root.getCompound(MEMORY_TAG);
    memory.remove("Pos" + hotbarSlot);
    memory.remove("Dim" + hotbarSlot);
    memory.remove("Slot" + hotbarSlot);
    root.put(MEMORY_TAG, memory);
  }

  public static BlockPos selectedBindingPos(ServerPlayer player) {
    int hotbarSlot = player.getInventory().selected;
    CompoundTag memory = player.getPersistentData().getCompound(MEMORY_TAG);
    if (!memory.contains("Pos" + hotbarSlot)) return null;
    if (!memory
        .getString("Dim" + hotbarSlot)
        .equals(player.level().dimension().location().toString())) return null;
    return BlockPos.of(memory.getLong("Pos" + hotbarSlot));
  }

  public static void clearSelectedMemory(ServerPlayer player) {
    int hotbarSlot = player.getInventory().selected;
    CompoundTag root = player.getPersistentData();
    CompoundTag memory = root.getCompound(MEMORY_TAG);
    memory.remove("Pos" + hotbarSlot);
    memory.remove("Dim" + hotbarSlot);
    memory.remove("Slot" + hotbarSlot);
    root.put(MEMORY_TAG, memory);
  }

  private int rememberedSlot(ServerPlayer player, int hotbarSlot) {
    RackBinding binding = binding(player, hotbarSlot);
    return binding != null && binding.pos().equals(worldPosition) ? binding.slot() : -1;
  }

  private void remember(ServerPlayer player, int hotbarSlot, int compartment) {
    CompoundTag root = player.getPersistentData();
    CompoundTag memory = root.getCompound(MEMORY_TAG);
    memory.putLong("Pos" + hotbarSlot, worldPosition.asLong());
    memory.putString("Dim" + hotbarSlot, player.level().dimension().location().toString());
    memory.putInt("Slot" + hotbarSlot, compartment);
    root.put(MEMORY_TAG, memory);
  }

  private RackBinding binding(ServerPlayer player, int hotbarSlot) {
    CompoundTag memory = player.getPersistentData().getCompound(MEMORY_TAG);
    String dimension = player.level().dimension().location().toString();
    if (!memory.contains("Pos" + hotbarSlot)
        || !memory.getString("Dim" + hotbarSlot).equals(dimension)) return null;
    int slot = memory.getInt("Slot" + hotbarSlot);
    return slot >= 0 && slot < COMPARTMENT_COUNT
        ? new RackBinding(BlockPos.of(memory.getLong("Pos" + hotbarSlot)), slot)
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
        || ItemStack.isSameItemSameComponents(stored, stack)
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

  private static boolean canFitInInventory(Inventory inventory, int excludedSlot, ItemStack stack) {
    int remaining = stack.getCount();
    for (int slot = 0; slot < 36 && remaining > 0; slot++) {
      if (slot == excludedSlot) continue;
      ItemStack stored = inventory.getItem(slot);
      if (stored.isEmpty()) remaining -= stack.getMaxStackSize();
      else if (ItemStack.isSameItemSameComponents(stored, stack))
        remaining -= stored.getMaxStackSize() - stored.getCount();
    }
    return remaining <= 0;
  }

  private static void storeInInventory(Inventory inventory, int excludedSlot, ItemStack stack) {
    for (int slot = 0; slot < 36 && !stack.isEmpty(); slot++) {
      if (slot == excludedSlot) continue;
      ItemStack stored = inventory.getItem(slot);
      if (!stored.isEmpty() && ItemStack.isSameItemSameComponents(stored, stack)) {
        int move = Math.min(stack.getCount(), stored.getMaxStackSize() - stored.getCount());
        stored.grow(move);
        stack.shrink(move);
      }
    }
    for (int slot = 0; slot < 36 && !stack.isEmpty(); slot++) {
      if (slot == excludedSlot || !inventory.getItem(slot).isEmpty()) continue;
      int move = Math.min(stack.getCount(), stack.getMaxStackSize());
      inventory.setItem(slot, stack.copyWithCount(move));
      stack.shrink(move);
    }
  }

  private int findMatchingSlotWithRoom(ItemStack stack) {
    for (int i = 0; i < COMPARTMENT_COUNT; i++) {
      ItemStack stored = getItem(i);
      ItemStack filter = filters.get(i);
      int room =
          stored.isEmpty() ? stack.getMaxStackSize() : stored.getMaxStackSize() - stored.getCount();
      if (!filter.isEmpty()
          && canPlaceItem(i, stack)
          && canShareCategory(filter, stack)
          && room > 0) return i;
    }
    return -1;
  }

  private int findReturnSlot(ItemStack stack, int excludedSlot) {
    for (int slot = 0; slot < COMPARTMENT_COUNT; slot++) {
      if (slot != excludedSlot && !filters.get(slot).isEmpty() && canReceiveReturned(slot, stack))
        return slot;
    }
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
    int displayLevel = Math.min(4, occupied);
    BlockState state = getBlockState();
    if (state.hasProperty(AdvancedRackBlock.SPICE_LEVEL)
        && state.getValue(AdvancedRackBlock.SPICE_LEVEL) != displayLevel) {
      level.setBlock(worldPosition, state.setValue(AdvancedRackBlock.SPICE_LEVEL, displayLevel), 3);
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    updateDisplayState();
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
    super.saveAdditional(tag, provider);
    ContainerHelper.saveAllItems(tag, items, provider);
    CompoundTag filterTag = new CompoundTag();
    ContainerHelper.saveAllItems(filterTag, filters, provider);
    tag.put("Filters", filterTag);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
    super.loadAdditional(tag, provider);
    items = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);
    ContainerHelper.loadAllItems(tag, items, provider);
    filters = NonNullList.withSize(COMPARTMENT_COUNT, ItemStack.EMPTY);
    if (tag.contains("Filters"))
      ContainerHelper.loadAllItems(tag.getCompound("Filters"), filters, provider);
    for (int i = 0; i < COMPARTMENT_COUNT; i++) {
      if (filters.get(i).isEmpty() && !items.get(i).isEmpty())
        filters.set(i, items.get(i).copyWithCount(1));
    }
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
    return saveWithoutMetadata(provider);
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  public void refreshAfterPlacement() {
    sync();
  }

  private static boolean canShareCategory(ItemStack first, ItemStack second) {
    if (!first.isStackable()
        && !second.isStackable()
        && first.isDamageableItem()
        && second.isDamageableItem()) {
      return first.getItem() == second.getItem();
    }
    if (OilPotCompat.isOilPot(first) && OilPotCompat.isOilPot(second))
      return first.getItem() == second.getItem() && OilPotCompat.getType(first).equals(OilPotCompat.getType(second));
    return ItemStack.isSameItemSameComponents(first, second);
  }

  private record RackBinding(BlockPos pos, int slot) {}
}
