package cn.breezeth.kaleidoscope_grilling.seasoning;

import cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Stores every bottle as a real ItemStack so pending/finished seasoning data survives stacking,
 * pickup, world save and client synchronization exactly like the original implementation.
 */
public final class SeasoningBottleBlockEntity extends BlockEntity {
  public static final int CAPACITY = 8;
  public static final int MAX_BOTTLES = 4;
  private static final String BOTTLES = "Items";

  private NonNullList<ItemStack> bottles = NonNullList.withSize(MAX_BOTTLES, ItemStack.EMPTY);

  public SeasoningBottleBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.SEASONING_BOTTLE.get(), pos, state);
  }

  public int count() {
    int count = 0;
    for (ItemStack stack : bottles) if (!stack.isEmpty()) count++;
    return count;
  }

  public List<ItemStack> bottles() {
    List<ItemStack> out = new ArrayList<>();
    for (ItemStack stack : bottles) if (!stack.isEmpty()) out.add(stack.copy());
    return out;
  }

  public ItemStack bottleAt(int index) {
    return index >= 0 && index < MAX_BOTTLES ? bottles.get(index) : ItemStack.EMPTY;
  }

  public ItemStack top() {
    for (int i = MAX_BOTTLES - 1; i >= 0; i--) {
      if (!bottles.get(i).isEmpty()) return bottles.get(i);
    }
    return ItemStack.EMPTY;
  }

  public List<String> ingredients() {
    return SeasoningData.get(top());
  }

  public boolean isFinished() {
    return top().is(ModItems.SPECIAL_SEASONING.get());
  }

  public int damage() {
    return isFinished() ? SeasoningData.getUses(top()) : 0;
  }

  public int variant() {
    return SeasoningData.getVariant(top());
  }

  public boolean hasBase() {
    return hasBase(ingredients());
  }

  public boolean canAdd(String id) {
    Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
    return item != null
        && !isFinished()
        && ingredients().size() < CAPACITY
        && SeasoningAutomationApi.isValidIngredient(new ItemStack(item));
  }

  public boolean add(String id) {
    if (!canAdd(id)) return false;
    int slot = count() - 1;
    if (slot < 0) return false;
    ItemStack stack = bottles.get(slot);
    List<String> values = SeasoningData.get(stack);
    values.add(id);
    SeasoningData.set(stack, values);
    promotePendingIfReady(slot, stack, values);
    sync();
    return true;
  }

  public boolean push(ItemStack stack) {
    int count = count();
    if (count >= MAX_BOTTLES || !isBottle(stack)) return false;
    bottles.set(count, stack.copyWithCount(1));
    sync();
    return true;
  }

  public ItemStack pop() {
    int count = count();
    if (count == 0) return ItemStack.EMPTY;
    ItemStack result = normalizeForPickup(bottles.get(count - 1));
    bottles.set(count - 1, ItemStack.EMPTY);
    sync();
    return result;
  }

  public void loadFrom(ItemStack stack) {
    bottles = NonNullList.withSize(MAX_BOTTLES, ItemStack.EMPTY);
    bottles.set(0, stack.copyWithCount(1));
    sync();
  }

  public static boolean isBottle(ItemStack stack) {
    return stack.is(ModItems.EMPTY_SEASONING_BOTTLE.get())
        || stack.is(ModItems.PENDING_SEASONING.get())
        || stack.is(ModItems.SPECIAL_SEASONING.get());
  }

  private void promotePendingIfReady(int slot, ItemStack stack, List<String> values) {
    if (!stack.is(ModItems.EMPTY_SEASONING_BOTTLE.get()) || !hasBase(values)) return;
    ItemStack pending = new ItemStack(ModItems.PENDING_SEASONING.get());
    SeasoningData.set(pending, values);
    bottles.set(slot, pending);
  }

  private static ItemStack normalizeForPickup(ItemStack stack) {
    List<String> values = SeasoningData.get(stack);
    if (!stack.is(ModItems.EMPTY_SEASONING_BOTTLE.get()) || !hasBase(values)) return stack.copy();
    ItemStack pending = new ItemStack(ModItems.PENDING_SEASONING.get());
    SeasoningData.set(pending, values);
    return pending;
  }

  private static boolean hasBase(List<String> values) {
    return values.contains("kaleidoscope_grilling:green_chili_powder")
        && values.contains("kaleidoscope_grilling:sichuan_pepper")
        && values.contains("kaleidoscope_grilling:onion_powder");
  }

  private void sync() {
    setChanged();
    if (level == null) return;
    int visualCount = Math.max(1, count());
    BlockState state = getBlockState();
    if (state.getValue(SeasoningBottleBlock.COUNT) != visualCount) {
      level.setBlock(worldPosition, state.setValue(SeasoningBottleBlock.COUNT, visualCount), Block.UPDATE_ALL);
    } else if (!level.isClientSide()) {
      level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
    }
  }

  @Override
  protected void saveAdditional(ValueOutput out) {
    super.saveAdditional(out);
    ValueOutput.TypedOutputList<ItemStackWithSlot> list = out.list(BOTTLES, ItemStackWithSlot.CODEC);
    for (int i = 0; i < bottles.size(); i++) {
      ItemStack stack = bottles.get(i);
      if (!stack.isEmpty()) list.add(new ItemStackWithSlot(i, stack));
    }
  }

  @Override
  protected void loadAdditional(ValueInput in) {
    super.loadAdditional(in);
    bottles = NonNullList.withSize(MAX_BOTTLES, ItemStack.EMPTY);
    for (ItemStackWithSlot entry : in.listOrEmpty(BOTTLES, ItemStackWithSlot.CODEC)) {
      if (entry.isValidInContainer(MAX_BOTTLES)) bottles.set(entry.slot(), entry.stack());
    }
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
