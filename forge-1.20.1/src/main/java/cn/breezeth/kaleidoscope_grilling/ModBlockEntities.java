package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KaleidoscopeGrilling.MOD_ID);
    public static final RegistryObject<BlockEntityType<GrillBlockEntity>> GRILL = BLOCK_ENTITIES.register("grill",
            () -> BlockEntityType.Builder.of(GrillBlockEntity::new, ModBlocks.GRILL.get()).build(null));
    public static final RegistryObject<BlockEntityType<SeasoningBottleBlockEntity>> SEASONING_BOTTLE = BLOCK_ENTITIES.register("seasoning_bottle",
            () -> BlockEntityType.Builder.of(SeasoningBottleBlockEntity::new, ModBlocks.SEASONING_BOTTLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BigVatBlockEntity>> BIG_VAT = BLOCK_ENTITIES.register("big_vat",
            () -> BlockEntityType.Builder.of(BigVatBlockEntity::new, ModBlocks.BIG_VAT.get()).build(null));
    public static final RegistryObject<BlockEntityType<OilPressBlockEntity>> OIL_PRESS = BLOCK_ENTITIES.register("oil_press",
            () -> BlockEntityType.Builder.of(OilPressBlockEntity::new, ModBlocks.OIL_PRESS.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedRackBlockEntity>> ADVANCED_RACK=BLOCK_ENTITIES.register("advanced_rack",()->BlockEntityType.Builder.of(AdvancedRackBlockEntity::new,ModBlocks.ADVANCED_RACK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SkewerRecipeBlockEntity>> SKEWER_RECIPE = BLOCK_ENTITIES.register("skewer_recipe",
            () -> BlockEntityType.Builder.of(SkewerRecipeBlockEntity::new, ModBlocks.SKEWER_RECIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<SkewerPlateBlockEntity>> SKEWER_PLATE = BLOCK_ENTITIES.register("skewer_plate",
            () -> BlockEntityType.Builder.of(SkewerPlateBlockEntity::new, ModBlocks.SKEWER_PLATE.get()).build(null));
    private ModBlockEntities() {}
}
