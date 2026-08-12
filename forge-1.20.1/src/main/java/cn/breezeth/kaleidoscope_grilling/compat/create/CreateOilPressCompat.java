package cn.breezeth.kaleidoscope_grilling.compat.create;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.ModFluids;
import cn.breezeth.kaleidoscope_grilling.OilPressContainerApi;
import cn.breezeth.kaleidoscope_grilling.OilPressContainerApi.Handler;
import cn.breezeth.kaleidoscope_grilling.OilPressContainerApi.Probe;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/** Create (机械动力) integration for the oil press. Safe to load without Create installed. */
public final class CreateOilPressCompat {
  public static void registerHandlers() {
    OilPressContainerApi.register(
        new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "create_fluid_tank"),
        new Handler() {
          @Override
          public Probe probe(Level level, BlockPos pos, int buckets) {
            IFluidHandler handler = fluidHandlerAt(level, pos);
            if (handler == null) return Probe.NOT_CONTAINER;
            FluidStack toInsert = new FluidStack(ModFluids.CANOLA_SOURCE.get(), buckets * 1000);
            int filled = handler.fill(toInsert, IFluidHandler.FluidAction.SIMULATE);
            if (filled <= 0) return Probe.INCOMPATIBLE;
            return filled >= toInsert.getAmount() ? Probe.READY : Probe.FULL;
          }

          @Override
          public boolean insert(Level level, BlockPos pos, int buckets) {
            IFluidHandler handler = fluidHandlerAt(level, pos);
            if (handler == null) return false;
            FluidStack toInsert = new FluidStack(ModFluids.CANOLA_SOURCE.get(), buckets * 1000);
            if (handler.fill(toInsert, IFluidHandler.FluidAction.SIMULATE) < toInsert.getAmount())
              return false;
            return handler.fill(toInsert, IFluidHandler.FluidAction.EXECUTE) == toInsert.getAmount();
          }
        });
  }

  /** Multiblock controller's fluid handler, or null when the position is not a Create fluid tank. */
  private static IFluidHandler fluidHandlerAt(Level level, BlockPos pos) {
    if (!(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank)) return null;
    FluidTankBlockEntity controller = tank.getControllerBE();
    if (controller == null) return null;
    LazyOptional<IFluidHandler> cap =
        controller.getCapability(ForgeCapabilities.FLUID_HANDLER, null);
    return cap.orElse(null);
  }

  /** Puts residue onto a Create belt in the 3x3 area two blocks below the press. Returns the remainder. */
  public static ItemStack tryInsertResidueToBelt(Level level, BlockPos pressPos, ItemStack residue) {
    if (residue.isEmpty()) return residue;
    // Input belts use the first layer below the press; residue targets the second layer.
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        BlockPos pos = pressPos.offset(dx, -2, dz);
        if (!(level.getBlockState(pos).getBlock() instanceof BeltBlock)) continue;
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) continue;
        LazyOptional<IItemHandler> cap =
            be.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        IItemHandler handler = cap.orElse(null);
        if (handler == null) continue;
        ItemStack remainder = handler.insertItem(0, residue, false);
        if (remainder.getCount() < residue.getCount()) return remainder;
      }
    }
    return residue;
  }

  /** For Jade: returns {current, capacity} in mB of the tank at pos, or null. */
  public static long[] fluidTankCapacity(Level level, BlockPos pos) {
    IFluidHandler handler = fluidHandlerAt(level, pos);
    if (handler == null) return null;
    long current = 0;
    for (int i = 0; i < handler.getTanks(); i++) current += handler.getFluidInTank(i).getAmount();
    long capacity = 0;
    for (int i = 0; i < handler.getTanks(); i++) capacity += handler.getTankCapacity(i);
    return new long[] {current, capacity};
  }

  private CreateOilPressCompat() {}
}
