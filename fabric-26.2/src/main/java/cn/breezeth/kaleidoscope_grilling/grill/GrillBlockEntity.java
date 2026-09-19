package cn.breezeth.kaleidoscope_grilling.grill;

import cn.breezeth.kaleidoscope_grilling.GrillAutomationApi;
import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import cn.breezeth.kaleidoscope_grilling.skewer.FailedSkewerData;
import cn.breezeth.kaleidoscope_grilling.skewer.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class GrillBlockEntity extends BlockEntity implements Container {
  public static final int SLOT_COUNT = 3;
  private static final int FINISHED_TICKS = 800;
  private static final int BURNT_TICKS = 400;

  private static final String ITEMS = "Items";
  private static final String PHASE = "Phase";
  private static final String PHASE_TICKS = "PhaseTicks";
  private static final String FLIPS = "Flips";
  private static final String FLIP_COOLDOWN = "FlipCooldown";
  private static final String HEAT_DURATION = "HeatDuration";
  private static final String AUTOMATION_OWNER = "AutomationOwner";
  private static final String AUTOMATION_HEARTBEAT = "AutomationHeartbeat";
  private static final String AUTOMATION_BLOCKED_OWNER = "AutomationBlockedOwner";
  private static final String AUTOMATION_BLOCKED_UNTIL = "AutomationBlockedUntil";
  private static final String SEASONED = "Seasoned";
  private static final String FAILED = "Failed";
  private static final String SEASONING_INGREDIENTS = "SeasoningIngredients";

  private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
  private int phase;
  private int phaseTicks;
  private int flips;
  private int flipCooldown;
  private boolean seasoned;
  private boolean failed;
  private int heatDurationTicks;
  private UUID automationOwner;
  private long automationHeartbeat;
  private UUID automationBlockedOwner;
  private long automationBlockedUntil;
  private final List<String> seasoningIngredients = new ArrayList<>();
  public final FlipAnimationData flipAnimationData = new FlipAnimationData();

  public GrillBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.GRILL.get(), pos, state);
  }

  public static void tick(Level level, BlockPos pos, BlockState state, GrillBlockEntity grill) {
    if (grill.automationOwner != null
        && grill.automationExpired(level.getGameTime(), GrillAutomationApi.DEFAULT_LEASE_TIMEOUT_TICKS)) {
      grill.clearAutomation();
      GrillAutomationApi.untrackExpiredLease(level, pos);
    }
    if (grill.flipCooldown > 0) grill.flipCooldown--;
    if (!state.getValue(GrillBlock.LIT) || grill.isEmpty()) return;

    grill.phaseTicks++;
    if (grill.phaseTicks % 20 == 0) grill.sync();
    if (grill.phase <= 2 && grill.phaseTicks >= FINISHED_TICKS) {
      grill.phase = 3;
      grill.phaseTicks = 0;
      grill.sync();
    } else if (grill.phase == 3 && grill.phaseTicks >= BURNT_TICKS) {
      Block.popResource(level, pos, new ItemStack(Items.CHARCOAL, 1 + level.random.nextInt(2)));
      grill.clearContent();
      grill.resetProcess();
      grill.sync();
    }
  }

  public boolean canAccept(ItemStack stack) {
    return phase == 0
        && items.stream().anyMatch(ItemStack::isEmpty)
        && (SkewerRecipes.isRawSkewer(stack)
            || (stack.is(ModItems.SECRET_SKEWER.get())
                && !SecretSkewerItem.isCooked(stack)
                && SkeweringHandler.ingredientCount(stack) == 3));
  }

  public boolean insert(ItemStack held, boolean consumeInput) {
    if (!canAccept(held)) return false;
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).isEmpty()) {
        items.set(i, held.copyWithCount(1));
        if (consumeInput) held.shrink(1);
        sync();
        return true;
      }
    }
    return false;
  }

  public void insert(ItemStack held, Player player) {
    insert(held, !player.getAbilities().instabuild);
  }

  public int brushOil(int heatTicks) {
    if (phase != 0 || isEmpty()) return 0;
    phase = 1;
    phaseTicks = 0;
    heatDurationTicks = heatTicks;
    sync();
    return occupiedSlots();
  }

  public boolean canFlip() {
    return phase == 1 && flipCooldown == 0;
  }

  public void flip() {
    if (!canFlip()) return;
    flips++;
    phaseTicks = 0;
    flipCooldown = 20;
    if (flips >= 4) {
      phase = 2;
      failed = false;
      if (level != null && !level.isClientSide()) {
        for (ItemStack item : items) SkeweringHandler.ensureCookedIngredientStacks(item, level);
      }
    }
    sync();
  }

  public int season(ItemStack seasoning) {
    if (phase != 2 || seasoned) return 0;
    seasoned = true;
    seasoningIngredients.clear();
    seasoningIngredients.addAll(SeasoningData.get(seasoning));
    sync();
    return occupiedSlots();
  }

  public int seasonableCount() {
    return phase == 2 && !seasoned ? occupiedSlots() : 0;
  }

  public boolean canExtractNormally() {
    return phase == 2 && seasoned && !failed;
  }

  public boolean canExtract() {
    return (phase == 2 && seasoned) || phase == 3;
  }

  public ItemStack extractOneStack(boolean simulate) {
    if (!canExtract()) return ItemStack.EMPTY;
    for (int i = 0; i < items.size(); i++) {
      ItemStack input = items.get(i);
      if (input.isEmpty()) continue;
      ItemStack output = cookedOutput(input);
      if (!simulate) {
        items.set(i, ItemStack.EMPTY);
        if (isEmpty()) resetProcess();
        sync();
      }
      return output;
    }
    return ItemStack.EMPTY;
  }

  public boolean extractOne(Player player) {
    ItemStack output = extractOneStack(false);
    if (output.isEmpty()) return false;
    player.getInventory().placeItemBackInInventory(output);
    return true;
  }

  public int extractAll(Player player) {
    int count = 0;
    while (canExtract() && !isEmpty() && extractOne(player)) count++;
    return count;
  }

  public void dropForBreak() {
    if (level == null || level.isClientSide()) return;
    for (ItemStack input : items) {
      if (input.isEmpty()) continue;
      ItemStack output;
      if (phase == 0) output = input.copy();
      else if (phase == 2 && seasoned && !failed) output = cookedOutput(input);
      else if (phase == 3) output = FailedSkewerData.create(input, ModItems.DARK_GRILLING.get());
      else output = FailedSkewerData.create(input, ModItems.MYSTERIOUS_SKEWER.get());
      Block.popResource(level, worldPosition, output);
    }
    items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    automationOwner = null;
    automationHeartbeat = 0;
    resetProcess();
    sync();
  }

  private ItemStack cookedOutput(ItemStack input) {
    ItemStack output;
    if (phase == 3) output = FailedSkewerData.create(input, ModItems.DARK_GRILLING.get());
    else if (failed) output = FailedSkewerData.create(input, ModItems.MYSTERIOUS_SKEWER.get());
    else if (input.is(ModItems.SECRET_SKEWER.get())) {
      output = input.copy();
      CustomData data = input.get(DataComponents.CUSTOM_DATA);
      if (data != null) output.set(DataComponents.CUSTOM_DATA, data);
      if (level != null) SkeweringHandler.ensureCookedIngredientStacks(output, level);
      SecretSkewerItem.setCooked(output, true);
      SeasoningData.set(output, seasoningIngredients);
      if (level != null) FoodState.setHot(output, level.getGameTime() + heatDurationTicks);
    } else {
      output = SkewerRecipes.cookedResult(input);
      if (output.isEmpty()) return FailedSkewerData.create(input, ModItems.MYSTERIOUS_SKEWER.get());
      CustomData data = input.get(DataComponents.CUSTOM_DATA);
      if (data != null) {
        CompoundTag merged = data.copyTag();
        CustomData generated = output.get(DataComponents.CUSTOM_DATA);
        if (generated != null) merged.merge(generated.copyTag());
        output.set(DataComponents.CUSTOM_DATA, CustomData.of(merged));
      }
      SeasoningData.set(output, seasoningIngredients);
      if (level != null) FoodState.setHot(output, level.getGameTime() + heatDurationTicks);
    }
    return output;
  }

  public boolean isFailed() { return failed; }
  public int getPhase() { return phase; }
  public int getFlips() { return flips; }
  public int getPhaseTicks() { return phaseTicks; }
  public int getFlipCooldown() { return flipCooldown; }
  public boolean isSeasoned() { return seasoned; }
  public int getHeatDurationTicks() { return heatDurationTicks; }
  public int occupiedSlots() { return (int) items.stream().filter(s -> !s.isEmpty()).count(); }

  public boolean tryAcquireAutomation(UUID owner, long gameTime, int timeoutTicks) {
    if (owner == null) return false;
    if (automationBlockedOwner != null && gameTime >= automationBlockedUntil) {
      automationBlockedOwner = null;
      automationBlockedUntil = 0;
    }
    if (owner.equals(automationBlockedOwner)) return false;
    if (automationOwner == null || automationOwner.equals(owner) || automationExpired(gameTime, timeoutTicks)) {
      automationOwner = owner;
      automationHeartbeat = gameTime;
      sync();
      return true;
    }
    return false;
  }

  public boolean heartbeatAutomation(UUID owner, long gameTime) {
    if (owner == null || !owner.equals(automationOwner)) return false;
    automationHeartbeat = gameTime;
    setChanged();
    return true;
  }

  public boolean releaseAutomation(UUID owner) {
    if (owner == null || !owner.equals(automationOwner)) return false;
    clearAutomation();
    return true;
  }

  public boolean forceReleaseAutomation() {
    return forceReleaseAutomation(level == null ? 0 : level.getGameTime(), 600);
  }

  public boolean forceReleaseAutomation(long gameTime, int blockTicks) {
    if (automationOwner == null) return false;
    automationBlockedOwner = automationOwner;
    automationBlockedUntil = gameTime + Math.max(1, blockTicks);
    clearAutomation();
    return true;
  }

  public UUID getAutomationOwner(long gameTime, int timeoutTicks) {
    if (automationExpired(gameTime, timeoutTicks)) {
      clearAutomation();
      if (level != null) GrillAutomationApi.untrackExpiredLease(level, worldPosition);
    }
    return automationOwner;
  }

  private boolean automationExpired(long gameTime, int timeoutTicks) {
    return automationOwner != null && gameTime - automationHeartbeat > Math.max(1, timeoutTicks);
  }

  private void clearAutomation() {
    automationOwner = null;
    automationHeartbeat = 0;
    sync();
  }

  private void resetProcess() {
    phase = phaseTicks = flips = flipCooldown = heatDurationTicks = 0;
    seasoned = failed = false;
    seasoningIngredients.clear();
  }

  private void sync() {
    setChanged();
    if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
  }

  @Override
  protected void saveAdditional(ValueOutput out) {
    super.saveAdditional(out);
    ValueOutput.TypedOutputList<ItemStackWithSlot> itemList = out.list(ITEMS, ItemStackWithSlot.CODEC);
    for (int i = 0; i < items.size(); i++) {
      ItemStack stack = items.get(i);
      if (!stack.isEmpty()) itemList.add(new ItemStackWithSlot(i, stack));
    }
    out.putInt(PHASE, phase);
    out.putInt(PHASE_TICKS, phaseTicks);
    out.putInt(FLIPS, flips);
    out.putInt(FLIP_COOLDOWN, flipCooldown);
    out.putInt(HEAT_DURATION, heatDurationTicks);
    if (automationOwner != null) out.putString(AUTOMATION_OWNER, automationOwner.toString());
    out.putLong(AUTOMATION_HEARTBEAT, automationHeartbeat);
    if (automationBlockedOwner != null) out.putString(AUTOMATION_BLOCKED_OWNER, automationBlockedOwner.toString());
    out.putLong(AUTOMATION_BLOCKED_UNTIL, automationBlockedUntil);
    out.putBoolean(SEASONED, seasoned);
    out.putBoolean(FAILED, failed);
    ValueOutput.TypedOutputList<String> seasonings = out.list(SEASONING_INGREDIENTS, Codec.STRING);
    seasoningIngredients.forEach(seasonings::add);
  }

  @Override
  protected void loadAdditional(ValueInput in) {
    super.loadAdditional(in);
    items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    for (ItemStackWithSlot entry : in.listOrEmpty(ITEMS, ItemStackWithSlot.CODEC)) {
      if (entry.isValidInContainer(items.size())) items.set(entry.slot(), entry.stack());
    }
    phase = in.getIntOr(PHASE, 0);
    phaseTicks = in.getIntOr(PHASE_TICKS, 0);
    flips = in.getIntOr(FLIPS, 0);
    flipCooldown = in.getIntOr(FLIP_COOLDOWN, 0);
    heatDurationTicks = in.getIntOr(HEAT_DURATION, 0);
    automationOwner = parseUuid(in.getString(AUTOMATION_OWNER).orElse(""));
    automationHeartbeat = in.getLongOr(AUTOMATION_HEARTBEAT, 0L);
    automationBlockedOwner = parseUuid(in.getString(AUTOMATION_BLOCKED_OWNER).orElse(""));
    automationBlockedUntil = in.getLongOr(AUTOMATION_BLOCKED_UNTIL, 0L);
    seasoned = in.getBooleanOr(SEASONED, false);
    failed = in.getBooleanOr(FAILED, false);
    seasoningIngredients.clear();
    for (String value : in.listOrEmpty(SEASONING_INGREDIENTS, Codec.STRING)) seasoningIngredients.add(value);
  }

  private static UUID parseUuid(String value) {
    if (value == null || value.isEmpty()) return null;
    try { return UUID.fromString(value); }
    catch (IllegalArgumentException ignored) { return null; }
  }

  @Override
  public int getContainerSize() { return SLOT_COUNT; }

  @Override
  public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }

  @Override
  public ItemStack getItem(int slot) { return items.get(slot); }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack result = ContainerHelper.removeItem(items, slot, amount);
    if (!result.isEmpty()) sync();
    return result;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }

  @Override
  public void setItem(int slot, ItemStack stack) {
    items.set(slot, stack);
    stack.limitSize(1);
    sync();
  }

  @Override
  public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }

  @Override
  public void clearContent() { items.clear(); }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return saveWithoutMetadata(registries);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && automationOwner != null) GrillAutomationApi.trackLoadedLease(level, worldPosition);
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  public static final class FlipAnimationData {
    public int observedFlips = -1;
    public long timestamp = -1L;
    public final float[] heights = new float[SLOT_COUNT];
  }
}
