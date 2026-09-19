package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Stores and synchronizes the five visible skewers on the serving plate. */
public final class SkewerPlateBlockEntity extends BlockEntity {
  public static final int CAPACITY = 5;
  private static final String SKEWERS = "Skewers";
  private NonNullList<ItemStack> skewers = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);

  public SkewerPlateBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.SKEWER_PLATE.get(), pos, state);
  }

  public int size() {
    int count = 0;
    for (ItemStack stack : skewers) if (!stack.isEmpty()) count++;
    return count;
  }

  public boolean add(ItemStack source, boolean creative) {
    if (!SkewerPlateItem.isSkewer(source) || size() >= CAPACITY) return false;
    for (int slot = 0; slot < CAPACITY; slot++) {
      if (!skewers.get(slot).isEmpty()) continue;
      skewers.set(slot, source.copyWithCount(1));
      if (!creative) source.shrink(1);
      sync();
      return true;
    }
    return false;
  }

  public ItemStack removeLast() {
    for (int slot = CAPACITY - 1; slot >= 0; slot--) {
      if (skewers.get(slot).isEmpty()) continue;
      ItemStack result = skewers.get(slot);
      skewers.set(slot, ItemStack.EMPTY);
      compact();
      sync();
      return result;
    }
    return ItemStack.EMPTY;
  }

  public List<ItemStack> copySkewers() {
    return skewers.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
  }

  public void setSkewers(List<ItemStack> values) {
    skewers = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    for (int i = 0; i < Math.min(CAPACITY, values.size()); i++) {
      skewers.set(i, values.get(i).copyWithCount(1));
    }
    sync();
  }

  public ItemStack getSkewer(int slot) {
    return slot >= 0 && slot < CAPACITY ? skewers.get(slot) : ItemStack.EMPTY;
  }

  private void compact() {
    NonNullList<ItemStack> compacted = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    int target = 0;
    for (ItemStack stack : skewers) if (!stack.isEmpty()) compacted.set(target++, stack);
    skewers = compacted;
  }

  private void sync() {
    setChanged();
    if (level != null) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
  }

  @Override
  protected void saveAdditional(ValueOutput out) {
    super.saveAdditional(out);
    ValueOutput.TypedOutputList<ItemStackWithSlot> list = out.list(SKEWERS, ItemStackWithSlot.CODEC);
    for (int i = 0; i < skewers.size(); i++) {
      ItemStack stack = skewers.get(i);
      if (!stack.isEmpty()) list.add(new ItemStackWithSlot(i, stack));
    }
  }

  @Override
  protected void loadAdditional(ValueInput in) {
    super.loadAdditional(in);
    skewers = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    for (ItemStackWithSlot entry : in.listOrEmpty(SKEWERS, ItemStackWithSlot.CODEC)) {
      if (entry.isValidInContainer(CAPACITY)) skewers.set(entry.slot(), entry.stack());
    }
    compact();
  }

  @Override
  public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
}
