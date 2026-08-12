package cn.breezeth.kaleidoscope_grilling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OilPressBlockEntity extends BlockEntity {
  public static final int MAX_CAKES = 4,
      REQUIRED_PROGRESS = 16,
      ANVIL_PROGRESS = 4,
      PRESS_COOLDOWN_TICKS = 10;
  private final Map<UUID, Long> playerCooldowns = new HashMap<>();
  private final List<PendingImpact> pendingImpacts = new ArrayList<>();
  private final IItemHandler oilCakeHandler = createOilCakeHandler();
  private LazyOptional<IItemHandler> oilCakeCapability = LazyOptional.of(() -> oilCakeHandler);
  private int cakes, progress, residue, completionDelay;
  private boolean waitingForContainer;
  private long lastFailureMessage = -100;

  public OilPressBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.OIL_PRESS.get(), pos, state);
  }

  public static void tick(
      net.minecraft.world.level.Level level,
      BlockPos pos,
      BlockState state,
      OilPressBlockEntity press) {
    if (press.completionDelay > 0) {
      press.completionDelay--;
      if (press.completionDelay == 0 && press.progress >= REQUIRED_PROGRESS) {
        press.tryFinish();
        press.sync();
      }
    }
    press.tickPendingImpacts(level);
  }

  public int cakes() {
    return cakes;
  }

  public int progress() {
    return progress;
  }

  public int residue() {
    return residue;
  }

  public boolean waitingForContainer() {
    return waitingForContainer;
  }

  /** Item handler for Create funnel insertion; refuses to accept when full. */
  public IItemHandler itemHandler() {
    return oilCakeHandler;
  }

  private IItemHandler createOilCakeHandler() {
    return new IItemHandler() {
      @Override
      public int getSlots() {
        return 1;
      }

      @Override
      public ItemStack getStackInSlot(int slot) {
        return slot == 0 && cakes > 0
            ? new ItemStack(ModItems.OIL_CAKE.get(), cakes)
            : ItemStack.EMPTY;
      }

      @Override
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || !stack.is(ModItems.OIL_CAKE.get()) || cakes >= MAX_CAKES) return stack;
        int accepted = Math.min(stack.getCount(), MAX_CAKES - cakes);
        if (!simulate) {
          cakes += accepted;
          sync();
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(accepted);
        return remainder;
      }

      @Override
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
      }

      @Override
      public int getSlotLimit(int slot) {
        return slot == 0 ? MAX_CAKES : 0;
      }

      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && stack.is(ModItems.OIL_CAKE.get());
      }
    };
  }

  public boolean addCake() {
    if (cakes >= MAX_CAKES) return false;
    cakes++;
    sync();
    return true;
  }

  public int takeResidue() {
    int value = residue;
    residue = 0;
    sync();
    return value;
  }

  public boolean pressWithTool(Player player, int amount) {
    if (level == null
        || cakes != MAX_CAKES
        || waitingForContainer
        || progress >= REQUIRED_PROGRESS
        || amount <= 0) return false;
    long now = level.getGameTime();
    if (now < playerCooldowns.getOrDefault(player.getUUID(), Long.MIN_VALUE)) return false;
    playerCooldowns.put(player.getUUID(), now + PRESS_COOLDOWN_TICKS);
    pendingImpacts.add(
        new PendingImpact(player.getUUID(), amount, AnvilPressAnimation.IMPACT_TICK));
    return true;
  }

  private void tickPendingImpacts(net.minecraft.world.level.Level level) {
    Iterator<PendingImpact> iterator = pendingImpacts.iterator();
    while (iterator.hasNext()) {
      PendingImpact pending = iterator.next();
      if (--pending.ticks > 0) continue;
      iterator.remove();
      if (!addProgress(pending.amount)) continue;
      level.playSound(
          null,
          worldPosition,
          SoundEvents.ANVIL_LAND,
          SoundSource.BLOCKS,
          1.15F,
          .82F + level.random.nextFloat() * .12F);
      if (level instanceof ServerLevel server) {
        server.sendParticles(
            ParticleTypes.CRIT,
            worldPosition.getX() + .5,
            worldPosition.getY() + .9,
            worldPosition.getZ() + .5,
            14,
            .32,
            .18,
            .32,
            .08);
        ServerPlayer player = server.getServer().getPlayerList().getPlayer(pending.playerId);
        if (player != null)
          player.displayClientMessage(
              Component.translatable(
                  "message.kaleidoscope_grilling.press_status", cakes, progress, REQUIRED_PROGRESS),
              true);
      }
    }
  }

  /** Public integration point for other mods. Amount uses the same 0-16 progress scale. */
  public boolean addProgress(int amount) {
    if (cakes != MAX_CAKES || amount <= 0 || waitingForContainer) return false;
    progress = Math.min(REQUIRED_PROGRESS, progress + amount);
    if (progress >= REQUIRED_PROGRESS && completionDelay == 0)
      completionDelay = PRESS_COOLDOWN_TICKS;
    sync();
    return true;
  }

  public InspectionResult inspectForContainer() {
    if (!waitingForContainer) return InspectionResult.NOT_WAITING;
    boolean completed = tryFinish();
    sync();
    return completed ? InspectionResult.COMPLETED : InspectionResult.WAITING;
  }

  private boolean tryFinish() {
    OilPressContainerApi.TransferResult result =
        OilPressContainerApi.insertNearby(level, worldPosition, MAX_CAKES);
    if (result.success()) {
      ItemStack residueRemainder =
          CreateCompat.insertResidue(
              level, worldPosition, new ItemStack(ModItems.OIL_RESIDUE.get(), MAX_CAKES));
      if (!residueRemainder.isEmpty()) ejectResidue(residueRemainder.getCount());
      cakes = 0;
      progress = 0;
      waitingForContainer = false;
      if (level != null)
        level.playSound(
            null, worldPosition, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, .8F, .9F);
      return true;
    }
    progress = REQUIRED_PROGRESS - 1;
    waitingForContainer = true;
    String key =
        switch (result.status()) {
          case FULL -> "message.kaleidoscope_grilling.press_vat_full";
          case INCOMPATIBLE -> "message.kaleidoscope_grilling.press_wrong_vat";
          default -> "message.kaleidoscope_grilling.press_no_vat";
        };
    failure(key);
    return false;
  }

  private void ejectResidue(int count) {
    if (level == null || count <= 0) return;
    Direction facing = getBlockState().getValue(OilPressBlock.FACING);
    double x = worldPosition.getX() + .5 + facing.getStepX() * .72,
        y = worldPosition.getY() + .58,
        z = worldPosition.getZ() + .5 + facing.getStepZ() * .72;
    ItemEntity item =
        new ItemEntity(level, x, y, z, new ItemStack(ModItems.OIL_RESIDUE.get(), count));
    item.setDeltaMovement(facing.getStepX() * .22, .18, facing.getStepZ() * .22);
    item.setDefaultPickUpDelay();
    level.addFreshEntity(item);
  }

  private void failure(String key) {
    if (level == null || level.getGameTime() - lastFailureMessage < 40) return;
    lastFailureMessage = level.getGameTime();
    level
        .getEntitiesOfClass(ServerPlayer.class, new AABB(worldPosition).inflate(6))
        .forEach(player -> player.displayClientMessage(Component.translatable(key), true));
  }

  private static int visualStage(int progress) {
    if (progress <= 0) return 0;
    if (progress <= 4) return 1;
    if (progress <= 8) return 2;
    if (progress <= 12) return 3;
    return 4;
  }

  @Override
  public <T> @NotNull LazyOptional<T> getCapability(
      @NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) return oilCakeCapability.cast();
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    oilCakeCapability.invalidate();
  }

  @Override
  public void reviveCaps() {
    super.reviveCaps();
    oilCakeCapability = LazyOptional.of(() -> oilCakeHandler);
  }

  private void sync() {
    setChanged();
    if (level == null || level.isClientSide) return;
    BlockState oldState = getBlockState();
    BlockState newState =
        oldState
            .setValue(OilPressBlock.CAKE_COUNT, cakes)
            .setValue(OilPressBlock.PRESS_STAGE, visualStage(progress));
    if (newState != oldState) level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
    level.sendBlockUpdated(worldPosition, newState, newState, Block.UPDATE_CLIENTS);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    sync();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("Cakes", cakes);
    tag.putInt("Progress", progress);
    tag.putInt("Residue", residue);
    tag.putInt("CompletionDelay", completionDelay);
    tag.putBoolean("WaitingForContainer", waitingForContainer);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    cakes = Math.max(0, Math.min(MAX_CAKES, tag.getInt("Cakes")));
    progress = Math.max(0, Math.min(REQUIRED_PROGRESS, tag.getInt("Progress")));
    residue = Math.max(0, tag.getInt("Residue"));
    completionDelay = Math.max(0, Math.min(PRESS_COOLDOWN_TICKS, tag.getInt("CompletionDelay")));
    waitingForContainer = tag.getBoolean("WaitingForContainer");
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = super.getUpdateTag();
    saveAdditional(tag);
    return tag;
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  public enum InspectionResult {
    NOT_WAITING,
    WAITING,
    COMPLETED
  }

  private static final class PendingImpact {
    private final UUID playerId;
    private final int amount;
    private int ticks;

    private PendingImpact(UUID playerId, int amount, int ticks) {
      this.playerId = playerId;
      this.amount = amount;
      this.ticks = ticks;
    }
  }
}
