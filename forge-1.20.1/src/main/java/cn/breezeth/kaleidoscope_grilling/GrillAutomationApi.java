package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Public, player-independent transaction API for grill automation. */
public final class GrillAutomationApi {
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
    int remaining = seasoning.getMaxDamage() - seasoning.getDamageValue();
    if (remaining < needed) return failure(Status.INSUFFICIENT_RESOURCE);
    if (!simulate && level.isClientSide) return failure(Status.CLIENT_SIDE);
    ItemStack replacement = ItemStack.EMPTY;
    if (!simulate) {
      int affected = grill.season(seasoning);
      if (affected != needed) return failure(Status.NOT_READY);
      if (consumeSeasoning) {
        int nextDamage = seasoning.getDamageValue() + affected;
        if (nextDamage >= seasoning.getMaxDamage()) {
          replacement = new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get());
        } else {
          seasoning.setDamageValue(nextDamage);
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

  private static GrillBlockEntity grillAt(Level level, BlockPos pos) {
    return level != null && level.getBlockEntity(pos) instanceof GrillBlockEntity grill
        ? grill
        : null;
  }

  private static boolean isRawSkewer(ItemStack stack) {
    return !stack.isEmpty()
        && (SkewerRecipes.isRawSkewer(stack)
            || (stack.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(stack)));
  }

  private static Result success(int affected) {
    return new Result(Status.SUCCESS, affected, ItemStack.EMPTY, ItemStack.EMPTY);
  }

  private static Result failure(Status status) {
    return new Result(status, 0, ItemStack.EMPTY, ItemStack.EMPTY);
  }
}
