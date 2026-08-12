package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BigVatBlockEntity extends BlockEntity {
  public static final int CAPACITY_BUCKETS = 8;
  public static final int BUCKET_VOLUME = FluidType.BUCKET_VOLUME;
  public static final int CAPACITY = CAPACITY_BUCKETS * BUCKET_VOLUME;

  private final FluidTank tank =
      new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
          sync();
        }
      };
  private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> tank);

  public BigVatBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.BIG_VAT.get(), pos, state);
  }

  public IFluidHandler fluidHandler() {
    return tank;
  }

  public FluidStack fluid() {
    return tank.getFluid();
  }

  public int amount() {
    return tank.getFluidAmount();
  }

  public int buckets() {
    return amount() / BUCKET_VOLUME;
  }

  public String content() {
    if (tank.isEmpty()) return "";
    Fluid fluid = tank.getFluid().getFluid();
    if (fluid == Fluids.WATER) return "water";
    if (fluid == ModFluids.CANOLA_SOURCE.get()) return "canola";
    ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
    return id == null ? "" : id.toString();
  }

  public String oilType() {
    if (tank.isEmpty()) return "";
    Fluid fluid = tank.getFluid().getFluid();
    if (fluid == ModFluids.CANOLA_SOURCE.get()) return "canola";
    if (fluid == ModFluids.SECRET_SOURCE.get()) return "secret_chili";
    if (fluid == ModFluids.PREMIUM_SOURCE.get()) return "premium_chili";
    return "";
  }

  public boolean canAccept(String type) {
    Fluid fluid = fluidFor(type);
    return fluid != Fluids.EMPTY && (tank.isEmpty() || tank.getFluid().getFluid() == fluid);
  }

  public boolean canInsert(Fluid fluid, int amount) {
    if (fluid == Fluids.EMPTY || amount <= 0) return false;
    return tank.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.SIMULATE) == amount;
  }

  public boolean insert(String type, int buckets) {
    Fluid fluid = fluidFor(type);
    int amount = buckets * BUCKET_VOLUME;
    return fluid != Fluids.EMPTY
        && amount > 0
        && tank.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE) == amount;
  }

  public boolean extract(String type, int buckets) {
    Fluid fluid = fluidFor(type);
    int amount = buckets * BUCKET_VOLUME;
    if (fluid == Fluids.EMPTY
        || amount <= 0
        || tank.getFluidAmount() < amount
        || tank.getFluid().getFluid() != fluid) return false;
    return tank.drain(amount, IFluidHandler.FluidAction.EXECUTE).getAmount() == amount;
  }

  private static Fluid fluidFor(String type) {
    if ("water".equals(type)) return Fluids.WATER;
    if ("canola".equals(type)) return ModFluids.CANOLA_SOURCE.get();
    if ("secret_chili".equals(type)) return ModFluids.SECRET_SOURCE.get();
    if ("premium_chili".equals(type)) return ModFluids.PREMIUM_SOURCE.get();
    ResourceLocation id = ResourceLocation.tryParse(type);
    return id == null ? Fluids.EMPTY : ForgeRegistries.FLUIDS.getValue(id);
  }

  private void sync() {
    setChanged();
    if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.put("Tank", tank.writeToNBT(new CompoundTag()));
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("Tank")) {
      tank.readFromNBT(tag.getCompound("Tank"));
      if (tank.getFluidAmount() > CAPACITY) {
        FluidStack limited = tank.getFluid().copy();
        limited.setAmount(CAPACITY);
        tank.setFluid(limited);
      }
    } else {
      Fluid legacy = fluidFor(tag.getString("Content"));
      int amount = Math.max(0, Math.min(CAPACITY_BUCKETS, tag.getInt("Buckets"))) * BUCKET_VOLUME;
      tank.setFluid(
          legacy == Fluids.EMPTY || amount == 0
              ? FluidStack.EMPTY
              : new FluidStack(legacy, amount));
    }
  }

  @Override
  public <T> @NotNull LazyOptional<T> getCapability(
      @NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCapability.cast();
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    fluidCapability.invalidate();
  }

  @Override
  public void reviveCaps() {
    super.reviveCaps();
    fluidCapability = LazyOptional.of(() -> tank);
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

  @Override
  public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
    CompoundTag tag = packet.getTag();
    if (tag != null) load(tag);
  }
}
