package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFluids {
  public static final DeferredRegister<FluidType> FLUID_TYPES =
      DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, KaleidoscopeGrilling.MOD_ID);
  public static final DeferredRegister<Fluid> FLUIDS =
      DeferredRegister.create(ForgeRegistries.FLUIDS, KaleidoscopeGrilling.MOD_ID);
  public static final RegistryObject<FluidType> CANOLA_TYPE =
      FLUID_TYPES.register(
          "canola_oil",
          () ->
              new OilFluidType(
                  FluidType.Properties.create().density(900).viscosity(1600), 0xFFC08A24));
  public static final RegistryObject<FluidType> SECRET_TYPE =
      FLUID_TYPES.register(
          "secret_chili_oil",
          () ->
              new OilFluidType(
                  FluidType.Properties.create().density(950).viscosity(1800), 0xFFE04B2A));
  public static final RegistryObject<FluidType> PREMIUM_TYPE =
      FLUID_TYPES.register(
          "premium_chili_oil",
          () ->
              new OilFluidType(
                  FluidType.Properties.create().density(1000).viscosity(2000).lightLevel(15),
                  0xFF9E1B16,
                  true));
  public static RegistryObject<FlowingFluid> CANOLA_SOURCE,
      CANOLA_FLOWING,
      SECRET_SOURCE,
      SECRET_FLOWING,
      PREMIUM_SOURCE,
      PREMIUM_FLOWING;
  public static RegistryObject<LiquidBlock> CANOLA_BLOCK, SECRET_BLOCK, PREMIUM_BLOCK;

  static {
    ForgeFlowingFluid.Properties a =
        new ForgeFlowingFluid.Properties(
                CANOLA_TYPE, () -> CANOLA_SOURCE.get(), () -> CANOLA_FLOWING.get())
            .bucket(() -> ModItems.CANOLA_OIL_BUCKET.get())
            .block(() -> CANOLA_BLOCK.get());
    CANOLA_SOURCE = FLUIDS.register("canola_oil", () -> new ForgeFlowingFluid.Source(a));
    CANOLA_FLOWING = FLUIDS.register("flowing_canola_oil", () -> new ForgeFlowingFluid.Flowing(a));
    CANOLA_BLOCK =
        ModBlocks.BLOCKS.register(
            "canola_oil",
            () ->
                new LiquidBlock(
                    () -> CANOLA_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                        .noCollission()
                        .strength(100)
                        .noLootTable()
                        .liquid()
                        .replaceable()));

    ForgeFlowingFluid.Properties b =
        new ForgeFlowingFluid.Properties(
                SECRET_TYPE, () -> SECRET_SOURCE.get(), () -> SECRET_FLOWING.get())
            .bucket(() -> ModItems.SECRET_CHILI_OIL_BUCKET.get())
            .block(() -> SECRET_BLOCK.get());
    SECRET_SOURCE = FLUIDS.register("secret_chili_oil", () -> new ForgeFlowingFluid.Source(b));
    SECRET_FLOWING =
        FLUIDS.register("flowing_secret_chili_oil", () -> new ForgeFlowingFluid.Flowing(b));
    SECRET_BLOCK =
        ModBlocks.BLOCKS.register(
            "secret_chili_oil",
            () ->
                new LiquidBlock(
                    () -> SECRET_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                        .noCollission()
                        .strength(100)
                        .noLootTable()
                        .liquid()
                        .replaceable()));

    ForgeFlowingFluid.Properties c =
        new ForgeFlowingFluid.Properties(
                PREMIUM_TYPE, () -> PREMIUM_SOURCE.get(), () -> PREMIUM_FLOWING.get())
            .bucket(() -> ModItems.PREMIUM_CHILI_OIL_BUCKET.get())
            .block(() -> PREMIUM_BLOCK.get());
    PREMIUM_SOURCE = FLUIDS.register("premium_chili_oil", () -> new ForgeFlowingFluid.Source(c));
    PREMIUM_FLOWING =
        FLUIDS.register("flowing_premium_chili_oil", () -> new ForgeFlowingFluid.Flowing(c));
    PREMIUM_BLOCK =
        ModBlocks.BLOCKS.register(
            "premium_chili_oil",
            () ->
                new PremiumChiliOilBlock(
                    () -> PREMIUM_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                        .noCollission()
                        .strength(100)
                        .noLootTable()
                        .liquid()
                        .replaceable()
                        .lightLevel(state -> 15)
                        .emissiveRendering((state, level, pos) -> true)));
  }

  private ModFluids() {}
}
