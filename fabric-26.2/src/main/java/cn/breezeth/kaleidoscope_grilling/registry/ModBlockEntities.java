package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;
import cn.breezeth.kaleidoscope_grilling.grill.GrillBlockEntity;
import cn.breezeth.kaleidoscope_grilling.oil.BigVatBlockEntity;
import cn.breezeth.kaleidoscope_grilling.oil.OilPressBlockEntity;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackBlockEntity;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningBottleBlockEntity;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerPlateBlockEntity;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipeBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
  public static final RegistryRef<BlockEntityType<GrillBlockEntity>> GRILL =
      register("grill", FabricBlockEntityTypeBuilder.create(GrillBlockEntity::new, ModBlocks.GRILL.get()).build());
  public static final RegistryRef<BlockEntityType<SeasoningBottleBlockEntity>> SEASONING_BOTTLE =
      register("seasoning_bottle", FabricBlockEntityTypeBuilder.create(SeasoningBottleBlockEntity::new, ModBlocks.SEASONING_BOTTLE.get()).build());
  public static final RegistryRef<BlockEntityType<BigVatBlockEntity>> BIG_VAT =
      register("big_vat", FabricBlockEntityTypeBuilder.create(BigVatBlockEntity::new, ModBlocks.BIG_VAT.get()).build());
  public static final RegistryRef<BlockEntityType<OilPressBlockEntity>> OIL_PRESS =
      register("oil_press", FabricBlockEntityTypeBuilder.create(OilPressBlockEntity::new, ModBlocks.OIL_PRESS.get()).build());
  public static final RegistryRef<BlockEntityType<AdvancedRackBlockEntity>> ADVANCED_RACK =
      register("advanced_rack", FabricBlockEntityTypeBuilder.create(AdvancedRackBlockEntity::new, ModBlocks.ADVANCED_RACK.get()).build());
  public static final RegistryRef<BlockEntityType<SkewerRecipeBlockEntity>> SKEWER_RECIPE =
      register("skewer_recipe", FabricBlockEntityTypeBuilder.create(SkewerRecipeBlockEntity::new, ModBlocks.SKEWER_RECIPE.get()).build());
  public static final RegistryRef<BlockEntityType<SkewerPlateBlockEntity>> SKEWER_PLATE =
      register("skewer_plate", FabricBlockEntityTypeBuilder.create(SkewerPlateBlockEntity::new, ModBlocks.SKEWER_PLATE.get()).build());

  private static <T extends BlockEntity> RegistryRef<BlockEntityType<T>> register(
      String name, BlockEntityType<T> type) {
    return RegistryRef.of(Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name),
        type));
  }

  public static void init() {}

  private ModBlockEntities() {}
}
