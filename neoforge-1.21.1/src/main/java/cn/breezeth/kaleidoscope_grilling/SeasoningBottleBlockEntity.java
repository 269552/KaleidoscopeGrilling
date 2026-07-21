package cn.breezeth.kaleidoscope_grilling;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SeasoningBottleBlockEntity extends BlockEntity {
  public static final int CAPACITY = 8;
  public static final int MAX_BOTTLES = 4;
  private static final Set<String> VALID =
      Set.of(
          "kaleidoscope_grilling:green_chili_powder",
          "kaleidoscope_grilling:sichuan_pepper",
          "kaleidoscope_grilling:onion_powder",
          "minecraft:redstone",
          "minecraft:gunpowder",
          "kaleidoscope_grilling:houttuynia_powder",
          "kaleidoscope_grilling:totem_powder",
          "kaleidoscope_grilling:dragon_egg_powder");
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

  public ItemStack top() {
    for (int i = MAX_BOTTLES - 1; i >= 0; i--) if (!bottles.get(i).isEmpty()) return bottles.get(i);
    return ItemStack.EMPTY;
  }

  public List<String> ingredients() {
    return SeasoningData.get(top());
  }

  public boolean isFinished() {
    return top().is(ModItems.SPECIAL_SEASONING.get());
  }

  public int damage() {
    return isFinished() ? top().getDamageValue() : 0;
  }

  public int variant() {
    return SeasoningData.getVariant(top());
  }

  public boolean hasBase() {
    List<String> values = ingredients();
    return values.contains("kaleidoscope_grilling:green_chili_powder")
        && values.contains("kaleidoscope_grilling:sichuan_pepper")
        && values.contains("kaleidoscope_grilling:onion_powder");
  }

  public boolean canAdd(String id) {
    return !isFinished() && ingredients().size() < CAPACITY && VALID.contains(id);
  }

  public boolean add(String id) {
    if (!canAdd(id)) return false;
    ItemStack stack = top();
    List<String> values = SeasoningData.get(stack);
    values.add(id);
    SeasoningData.set(stack, values);
    promotePendingIfReady(count() - 1, stack, values);
    sync();
    return true;
  }

  public boolean push(ItemStack stack) {
    if (count() >= MAX_BOTTLES || !isBottle(stack)) return false;
    bottles.set(count(), stack.copyWithCount(1));
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
    if (!stack.is(ModItems.EMPTY_SEASONING_BOTTLE.get()) || !hasBase(values)) return stack;
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
    if (state.getValue(SeasoningBottleBlock.COUNT) != visualCount)
      level.setBlock(worldPosition, state.setValue(SeasoningBottleBlock.COUNT, visualCount), 3);
    else if (!level.isClientSide) level.sendBlockUpdated(worldPosition, state, state, 3);
  }

  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    ContainerHelper.saveAllItems(tag, bottles, registries);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    bottles = NonNullList.withSize(MAX_BOTTLES, ItemStack.EMPTY);
    if (tag.contains("Items")) ContainerHelper.loadAllItems(tag, bottles, registries);
    else loadLegacy(tag);
  }

  private void loadLegacy(CompoundTag tag) {
    List<String> ingredients = new ArrayList<>();
    ListTag list = tag.getList("Ingredients", 8);
    for (int i = 0; i < list.size(); i++) ingredients.add(list.getString(i));
    ItemStack stack =
        new ItemStack(
            tag.getBoolean("Finished")
                ? ModItems.SPECIAL_SEASONING.get()
                : ingredients.isEmpty()
                    ? ModItems.EMPTY_SEASONING_BOTTLE.get()
                    : ModItems.PENDING_SEASONING.get());
    SeasoningData.set(stack, ingredients);
    if (stack.is(ModItems.SPECIAL_SEASONING.get())) {
      stack.setDamageValue(tag.getInt("Damage"));
      SeasoningData.setVariant(stack, tag.getInt("Variant"));
    }
    bottles.set(0, stack);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag tag = super.getUpdateTag(registries);
    saveAdditional(tag, registries);
    return tag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public void onDataPacket(
      Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
    CompoundTag tag = packet.getTag();
    if (tag != null) loadAdditional(tag, registries);
  }
}
