package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, KaleidoscopeGrilling.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, KaleidoscopeGrilling.MOD_ID);
    public static final DeferredHolder<FluidType, FluidType> CANOLA_TYPE = FLUID_TYPES.register("canola_oil", () -> new OilFluidType(FluidType.Properties.create().density(900).viscosity(1600), 0xFFC08A24));
    public static final DeferredHolder<FluidType, FluidType> SECRET_TYPE = FLUID_TYPES.register("secret_chili_oil", () -> new OilFluidType(FluidType.Properties.create().density(950).viscosity(1800), 0xFFE04B2A));
    public static final DeferredHolder<FluidType, FluidType> PREMIUM_TYPE = FLUID_TYPES.register("premium_chili_oil", () -> new OilFluidType(FluidType.Properties.create().density(1000).viscosity(2000).lightLevel(15), 0xFF9E1B16, true));
    public static DeferredHolder<Fluid, FlowingFluid> CANOLA_SOURCE, CANOLA_FLOWING, SECRET_SOURCE, SECRET_FLOWING, PREMIUM_SOURCE, PREMIUM_FLOWING;
    public static DeferredHolder<net.minecraft.world.level.block.Block, LiquidBlock> CANOLA_BLOCK, SECRET_BLOCK, PREMIUM_BLOCK;

    static {
        ResourceLocation aId = ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "canola_oil");
        BaseFlowingFluid.Properties a = new BaseFlowingFluid.Properties(CANOLA_TYPE, DeferredHolder.create(Registries.FLUID, aId), DeferredHolder.create(Registries.FLUID, aId.withPrefix("flowing_"))).bucket(DeferredHolder.create(Registries.ITEM, aId.withSuffix("_bucket"))).block(DeferredHolder.create(Registries.BLOCK, aId));
        CANOLA_SOURCE = FLUIDS.register("canola_oil", () -> new BaseFlowingFluid.Source(a));
        CANOLA_FLOWING = FLUIDS.register("flowing_canola_oil", () -> new BaseFlowingFluid.Flowing(a));
        CANOLA_BLOCK = ModBlocks.BLOCKS.register("canola_oil", () -> new LiquidBlock(CANOLA_SOURCE.get(), BlockBehaviour.Properties.of().noCollission().strength(100).noLootTable().liquid().replaceable()));

        ResourceLocation bId = ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "secret_chili_oil");
        BaseFlowingFluid.Properties b = new BaseFlowingFluid.Properties(SECRET_TYPE, DeferredHolder.create(Registries.FLUID, bId), DeferredHolder.create(Registries.FLUID, bId.withPrefix("flowing_"))).bucket(DeferredHolder.create(Registries.ITEM, bId.withSuffix("_bucket"))).block(DeferredHolder.create(Registries.BLOCK, bId));
        SECRET_SOURCE = FLUIDS.register("secret_chili_oil", () -> new BaseFlowingFluid.Source(b));
        SECRET_FLOWING = FLUIDS.register("flowing_secret_chili_oil", () -> new BaseFlowingFluid.Flowing(b));
        SECRET_BLOCK = ModBlocks.BLOCKS.register("secret_chili_oil", () -> new LiquidBlock(SECRET_SOURCE.get(), BlockBehaviour.Properties.of().noCollission().strength(100).noLootTable().liquid().replaceable()));

        ResourceLocation cId = ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "premium_chili_oil");
        BaseFlowingFluid.Properties c = new BaseFlowingFluid.Properties(PREMIUM_TYPE, DeferredHolder.create(Registries.FLUID, cId), DeferredHolder.create(Registries.FLUID, cId.withPrefix("flowing_"))).bucket(DeferredHolder.create(Registries.ITEM, cId.withSuffix("_bucket"))).block(DeferredHolder.create(Registries.BLOCK, cId));
        PREMIUM_SOURCE = FLUIDS.register("premium_chili_oil", () -> new BaseFlowingFluid.Source(c));
        PREMIUM_FLOWING = FLUIDS.register("flowing_premium_chili_oil", () -> new BaseFlowingFluid.Flowing(c));
        PREMIUM_BLOCK = ModBlocks.BLOCKS.register("premium_chili_oil", () -> new PremiumChiliOilBlock(PREMIUM_SOURCE.get(), BlockBehaviour.Properties.of().noCollission().strength(100).noLootTable().liquid().replaceable().lightLevel(state -> 15).emissiveRendering((state, level, pos) -> true)));
    }

    private ModFluids() {}
}
