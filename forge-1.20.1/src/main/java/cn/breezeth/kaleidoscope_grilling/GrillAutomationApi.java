package cn.breezeth.kaleidoscope_grilling;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import cn.breezeth.kaleidoscope_grilling.grill.GrillBlock;
import cn.breezeth.kaleidoscope_grilling.grill.GrillBlockEntity;
import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import cn.breezeth.kaleidoscope_grilling.skewer.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;


import java.util.UUID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Public, player-independent transaction API for grill automation. */
public final class GrillAutomationApi {
  public static final int DEFAULT_LEASE_TIMEOUT_TICKS = 200;
  private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_LEASES = new HashMap<>();

  private GrillAutomationApi() {}

  public enum Status {
    SUCCESS,
    INVALID_TARGET,
    CLIENT_SIDE,
    NOT_LIT,
    INVALID_INPUT,
    FULL,
    EMPTY,
    WRONG_PHASE,
    COOLDOWN,
    INSUFFICIENT_RESOURCE,
    NOT_READY
  }

  public record Snapshot(
      boolean lit,
      int phase,
      int flips,
      int occupied,
      int flipCooldown,
      boolean seasoned,
      boolean failed,
      UUID automationOwner) {}

  public record Result(Status status, int affected, ItemStack output, ItemStack heldReplacement) {
    public boolean success() {
      return status == Status.SUCCESS;
    }

    public boolean shouldReplaceHeld() {
      return !heldReplacement.isEmpty();
    }
  }

  public static Result insertSkewer(Level level, BlockPos pos, ItemStack source, boolean simulate) {
    return insertSkewer(level, pos, source, simulate, true);
  }

  public static Result insertSkewer(
      Level level, BlockPos pos, ItemStack source, boolean simulate, boolean consumeInput) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (!isRawSkewer(source)) return failure(Status.INVALID_INPUT);
    if (!level.getBlockState(pos).getValue(GrillBlock.LIT)) return failure(Status.NOT_LIT);
    if (grill.getPhase() != 0) return failure(Status.WRONG_PHASE);
    if (grill.occupiedSlots() >= grill.getContainerSize()) return failure(Status.FULL);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    if (!simulate && !grill.insert(source, consumeInput)) return failure(Status.NOT_READY);
    return success(1);
  }

  public static Result brushOil(Level level, BlockPos pos, ItemStack oilPot, boolean simulate) {
    return brushOil(level, pos, oilPot, simulate, true);
  }

  public static Result brushOil(
      Level level, BlockPos pos, ItemStack oilPot, boolean simulate, boolean consumeOil) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (!OilPotCompat.isOilPot(oilPot)) return failure(Status.INVALID_INPUT);
    if (grill.getPhase() != 0) return failure(Status.WRONG_PHASE);
    int needed = grill.occupiedSlots();
    if (needed == 0) return failure(Status.EMPTY);
    if (OilPotCompat.getCount(oilPot) < needed) return failure(Status.INSUFFICIENT_RESOURCE);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    if (!simulate) {
      int affected = grill.brushOil(OilPotCompat.heatDuration(oilPot));
      if (affected != needed) return failure(Status.NOT_READY);
      if (consumeOil) OilPotCompat.consume(oilPot, affected);
    }
    return success(needed);
  }

  public static Result flip(Level level, BlockPos pos, boolean simulate) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (grill.getPhase() != 1) return failure(Status.WRONG_PHASE);
    if (grill.getFlipCooldown() > 0) return failure(Status.COOLDOWN);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    if (!simulate) grill.flip();
    return success(grill.occupiedSlots());
  }

  public static Result season(Level level, BlockPos pos, ItemStack seasoning, boolean simulate) {
    return season(level, pos, seasoning, simulate, true);
  }

  public static Result season(
      Level level, BlockPos pos, ItemStack seasoning, boolean simulate, boolean consumeSeasoning) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (!seasoning.is(ModItems.SPECIAL_SEASONING.get())) return failure(Status.INVALID_INPUT);
    if (grill.getPhase() != 2 || grill.isSeasoned()) return failure(Status.WRONG_PHASE);
    int needed = grill.occupiedSlots();
    if (needed == 0) return failure(Status.EMPTY);
    int remaining = SeasoningData.MAX_USES - SeasoningData.getUses(seasoning);
    if (remaining < needed) return failure(Status.INSUFFICIENT_RESOURCE);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    ItemStack replacement = ItemStack.EMPTY;
    if (!simulate) {
      int affected = grill.season(seasoning);
      if (affected != needed) return failure(Status.NOT_READY);
      if (consumeSeasoning) {
        int nextUses = SeasoningData.getUses(seasoning) + affected;
        if (nextUses >= SeasoningData.MAX_USES) {
          replacement = new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get());
        } else {
          SeasoningData.setUses(seasoning, nextUses);
        }
      }
    }
    return new Result(Status.SUCCESS, needed, ItemStack.EMPTY, replacement);
  }

  public static Result extract(Level level, BlockPos pos, boolean simulate) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (!grill.canExtract()) return failure(Status.NOT_READY);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    ItemStack output = grill.extractOneStack(simulate);
    if (output.isEmpty()) return failure(Status.EMPTY);
    return new Result(Status.SUCCESS, 1, output, ItemStack.EMPTY);
  }

  public static Snapshot snapshot(Level level, BlockPos pos) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return null;
    return new Snapshot(
        level.getBlockState(pos).getValue(GrillBlock.LIT),
        grill.getPhase(),
        grill.getFlips(),
        grill.occupiedSlots(),
        grill.getFlipCooldown(),
        grill.isSeasoned(),
        grill.isFailed(),
        grill.getAutomationOwner(level.getGameTime(), DEFAULT_LEASE_TIMEOUT_TICKS));
  }

  public static Result ignite(
      Level level, BlockPos pos, ItemStack flintAndSteel, LivingEntity actor, boolean simulate) {
    return ignite(level, pos, flintAndSteel, actor, EquipmentSlot.MAINHAND, simulate);
  }

  public static Result ignite(
      Level level,
      BlockPos pos,
      ItemStack flintAndSteel,
      LivingEntity actor,
      EquipmentSlot slot,
      boolean simulate) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (!flintAndSteel.is(Items.FLINT_AND_STEEL)) return failure(Status.INVALID_INPUT);
    if (level.getBlockState(pos).getValue(GrillBlock.LIT)) return success(0);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    if (!simulate) {
      level.setBlock(
          pos, level.getBlockState(pos).setValue(GrillBlock.LIT, true), Block.UPDATE_ALL);
      level.playSound(
          null,
          pos,
          SoundEvents.FLINTANDSTEEL_USE,
          SoundSource.BLOCKS,
          1.0F,
          level.random.nextFloat() * 0.4F + 0.8F);
      if (actor != null) {
        flintAndSteel.hurtAndBreak(1, actor, entity -> entity.broadcastBreakEvent(slot));
      }
      else if (flintAndSteel.isDamageableItem()) {
        int nextDamage = flintAndSteel.getDamageValue() + 1;
        if (nextDamage >= flintAndSteel.getMaxDamage()) flintAndSteel.shrink(1);
        else flintAndSteel.setDamageValue(nextDamage);
      }
    }
    return success(1);
  }

  public static Result extinguish(Level level, BlockPos pos, boolean simulate) {
    GrillBlockEntity grill = grillAt(level, pos);
    if (grill == null) return failure(Status.INVALID_TARGET);
    if (!level.getBlockState(pos).getValue(GrillBlock.LIT)) return success(0);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    if (!simulate) {
      level.setBlock(
          pos, level.getBlockState(pos).setValue(GrillBlock.LIT, false), Block.UPDATE_ALL);
      level.playSound(
          null,
          pos,
          SoundEvents.FIRE_EXTINGUISH,
          SoundSource.BLOCKS,
          0.5F,
          2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
    }
    return success(1);
  }

  public static boolean acquire(Level level, BlockPos pos, UUID owner) {
    GrillBlockEntity grill = grillAt(level, pos);
    boolean acquired = grill != null
        && !level.isClientSide
        && grill.tryAcquireAutomation(owner, level.getGameTime(), DEFAULT_LEASE_TIMEOUT_TICKS);
    if (acquired) trackLease(level, pos);
    return acquired;
  }

  public static boolean heartbeat(Level level, BlockPos pos, UUID owner) {
    GrillBlockEntity grill = grillAt(level, pos);
    return grill != null
        && !level.isClientSide
        && grill.heartbeatAutomation(owner, level.getGameTime());
  }

  public static boolean release(Level level, BlockPos pos, UUID owner) {
    GrillBlockEntity grill = grillAt(level, pos);
    boolean released = grill != null && !level.isClientSide && grill.releaseAutomation(owner);
    if (released) untrackLease(level, pos);
    return released;
  }

  public static boolean forceUnlock(Level level, BlockPos pos) {
    GrillBlockEntity grill = grillAt(level, pos);
    boolean released =
        grill != null
            && !level.isClientSide
            && grill.forceReleaseAutomation(level.getGameTime(), 600);
    if (released) untrackLease(level, pos);
    return released;
  }

  public static int forceUnlockAll(MinecraftServer server) {
    int released = 0;
    Map<ResourceKey<Level>, Set<BlockPos>> snapshot;
    synchronized (ACTIVE_LEASES) {
      snapshot = new HashMap<>();
      ACTIVE_LEASES.forEach((dimension, positions) -> snapshot.put(dimension, Set.copyOf(positions)));
    }
    for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : snapshot.entrySet()) {
      Level level = server.getLevel(entry.getKey());
      if (level == null) continue;
      for (BlockPos pos : entry.getValue()) {
        if (level.hasChunkAt(pos) && forceUnlock(level, pos)) released++;
      }
    }
    return released;
  }

  public static void trackLoadedLease(Level level, BlockPos pos) {
    if (!level.isClientSide) trackLease(level, pos);
  }

  public static void untrackExpiredLease(Level level, BlockPos pos) {
    if (!level.isClientSide) untrackLease(level, pos);
  }

  private static void trackLease(Level level, BlockPos pos) {
    synchronized (ACTIVE_LEASES) {
      ACTIVE_LEASES.computeIfAbsent(level.dimension(), ignored -> new HashSet<>()).add(pos.immutable());
    }
  }

  private static void untrackLease(Level level, BlockPos pos) {
    synchronized (ACTIVE_LEASES) {
      Set<BlockPos> positions = ACTIVE_LEASES.get(level.dimension());
      if (positions == null) return;
      positions.remove(pos);
      if (positions.isEmpty()) ACTIVE_LEASES.remove(level.dimension());
    }
  }

  private static GrillBlockEntity grillAt(Level level, BlockPos pos) {
    return level != null && level.getBlockEntity(pos) instanceof GrillBlockEntity grill
        ? grill
        : null;
  }

  private static boolean isRawSkewer(ItemStack stack) {
    return !stack.isEmpty()
        && (SkewerRecipes.isRawSkewer(stack)
            || (stack.is(ModItems.SECRET_SKEWER.get())
                && !SecretSkewerItem.isCooked(stack)
                && SkeweringHandler.ingredientCount(stack) == 3));
  }

  public static boolean acceptsRawSkewer(ItemStack stack) {
    return isRawSkewer(stack);
  }

  private static Result success(int affected) {
    return new Result(Status.SUCCESS, affected, ItemStack.EMPTY, ItemStack.EMPTY);
  }

  private static Result failure(Status status) {
    return new Result(status, 0, ItemStack.EMPTY, ItemStack.EMPTY);
  }
}
