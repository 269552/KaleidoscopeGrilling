package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, KaleidoscopeGrilling.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrillBlockEntity>> GRILL = BLOCK_ENTITIES.register("grill",
            () -> BlockEntityType.Builder.of(GrillBlockEntity::new, ModBlocks.GRILL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SeasoningBottleBlockEntity>> SEASONING_BOTTLE = BLOCK_ENTITIES.register("seasoning_bottle",
            () -> BlockEntityType.Builder.of(SeasoningBottleBlockEntity::new, ModBlocks.SEASONING_BOTTLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BigVatBlockEntity>> BIG_VAT = BLOCK_ENTITIES.register("big_vat",
            () -> BlockEntityType.Builder.of(BigVatBlockEntity::new, ModBlocks.BIG_VAT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OilPressBlockEntity>> OIL_PRESS = BLOCK_ENTITIES.register("oil_press",()->BlockEntityType.Builder.of(OilPressBlockEntity::new,ModBlocks.OIL_PRESS.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AdvancedRackBlockEntity>> ADVANCED_RACK=BLOCK_ENTITIES.register("advanced_rack",()->BlockEntityType.Builder.of(AdvancedRackBlockEntity::new,ModBlocks.ADVANCED_RACK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkewerRecipeBlockEntity>> SKEWER_RECIPE = BLOCK_ENTITIES.register("skewer_recipe",
            () -> BlockEntityType.Builder.of(SkewerRecipeBlockEntity::new, ModBlocks.SKEWER_RECIPE.get()).build(null));
    private ModBlockEntities() {}
}
