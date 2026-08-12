package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
  private static final SoundType COOKERY_POT_SOUND =
      new SoundType(
          1.0F,
          0.8F,
          SoundEvents.LANTERN_BREAK,
          SoundEvents.LANTERN_STEP,
          SoundEvents.LANTERN_PLACE,
          SoundEvents.LANTERN_HIT,
          SoundEvents.LANTERN_FALL);
  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(BuiltInRegistries.BLOCK, KaleidoscopeGrilling.MOD_ID);
  public static final DeferredHolder<Block, Block> GRILL =
      BLOCKS.register(
          "grill",
          () ->
              new GrillBlock(
                  BlockBehaviour.Properties.ofLegacyCopy(Blocks.IRON_BARS)
                      .sound(COOKERY_POT_SOUND)
                      .strength(3.0F)
                      .lightLevel(s -> s.getValue(GrillBlock.LIT) ? 7 : 0)
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> SEASONING_BOTTLE =
      BLOCKS.register(
          "seasoning_bottle",
          () ->
              new SeasoningBottleBlock(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                      .sound(seasoningBottleSound())
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> BIG_VAT =
      BLOCKS.register(
          "big_vat",
          () ->
              new BigVatBlock(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS)
                      .strength(2.0F)
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> OIL_PRESS =
      BLOCKS.register(
          "oil_press",
          () ->
              new OilPressBlock(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                      .strength(2.5F)
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> ADVANCED_RACK =
      BLOCKS.register(
          "advanced_rack",
          () ->
              new AdvancedRackBlock(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)
                      .strength(2.0F)
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> SKEWER_RECIPE =
      BLOCKS.register(
          "skewer_recipe",
          () ->
              new SkewerRecipeBlock(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> SKEWER_PLATE =
      BLOCKS.register(
          "skewer_plate",
          () ->
              new SkewerPlateBlock(
                  BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> CANOLA_CROP =
      BLOCKS.register(
          "canola_crop",
          () ->
              new CanolaCropBlock(
                  BlockBehaviour.Properties.ofLegacyCopy(Blocks.WHEAT)
                      .noCollission()
                      .randomTicks()
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> ONION_CROP =
      BLOCKS.register(
          "onion_crop",
          () ->
              new OnionCropBlock(
                  BlockBehaviour.Properties.ofLegacyCopy(Blocks.WHEAT)
                      .noCollission()
                      .randomTicks()
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> SWEET_POTATO_CROP =
      BLOCKS.register(
          "sweet_potato_crop",
          () ->
              new SweetPotatoCropBlock(
                  BlockBehaviour.Properties.ofLegacyCopy(Blocks.BEETROOTS)
                      .noCollission()
                      .randomTicks()
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> HOUTTUYNIA_CROP =
      BLOCKS.register(
          "houttuynia_crop",
          () ->
              new HouttuyniaCropBlock(
                  BlockBehaviour.Properties.ofLegacyCopy(Blocks.NETHER_WART)
                      .noCollission()
                      .randomTicks()
                      .instabreak()
                      .noOcclusion()));
  public static final DeferredHolder<Block, Block> PEPPER_LOG =
      BLOCKS.register("pepper_log", PepperLogBlock::create);
  public static final DeferredHolder<Block, Block> PEPPER_LEAVES =
      BLOCKS.register("pepper_leaves", PepperLeavesBlock::create);
  public static final DeferredHolder<Block, Block> PEPPER_SAPLING =
      BLOCKS.register("pepper_sapling", PepperSaplingBlock::create);
  public static final DeferredHolder<Item, Item> PEPPER_LOG_ITEM =
      ModItems.ITEMS.register(
          "pepper_log", () -> new BlockItem(PEPPER_LOG.get(), new Item.Properties()));
  public static final DeferredHolder<Item, Item> PEPPER_LEAVES_ITEM =
      ModItems.ITEMS.register(
          "pepper_leaves", () -> new BlockItem(PEPPER_LEAVES.get(), new Item.Properties()));
  public static final DeferredHolder<Item, Item> PEPPER_SAPLING_ITEM =
      ModItems.ITEMS.register(
          "pepper_sapling", () -> new BlockItem(PEPPER_SAPLING.get(), new Item.Properties()));
  public static final DeferredHolder<Item, Item> GRILL_ITEM =
      ModItems.ITEMS.register(
          "grill",
          () ->
              new FunctionalBlockItem(
                  GRILL.get(),
                  new Item.Properties(),
                  "tooltip.kaleidoscope_grilling.grill.usage",
                  null));
  public static final DeferredHolder<Item, Item> BIG_VAT_ITEM =
      ModItems.ITEMS.register(
          "big_vat",
          () ->
              new FunctionalBlockItem(
                  BIG_VAT.get(),
                  new Item.Properties(),
                  "tooltip.kaleidoscope_grilling.big_vat.usage",
                  "tooltip.kaleidoscope_grilling.big_vat.requirement"));
  public static final DeferredHolder<Item, Item> OIL_PRESS_ITEM =
      ModItems.ITEMS.register(
          "oil_press",
          () ->
              new FunctionalBlockItem(
                  OIL_PRESS.get(),
                  new Item.Properties(),
                  "tooltip.kaleidoscope_grilling.oil_press.usage",
                  "tooltip.kaleidoscope_grilling.oil_press.requirement"));
  public static final DeferredHolder<Item, Item> ADVANCED_RACK_ITEM =
      ModItems.ITEMS.register(
          "advanced_rack",
          () -> new AdvancedRackBlockItem(ADVANCED_RACK.get(), new Item.Properties()));

  private static SoundType seasoningBottleSound() {
    return new SoundType(
        1.0F,
        1.0F,
        ModSounds.SEASONING_BOTTLE_PLACE.get(),
        SoundEvents.GLASS_STEP,
        ModSounds.SEASONING_BOTTLE_PLACE.get(),
        ModSounds.SEASONING_BOTTLE_PLACE.get(),
        SoundEvents.GLASS_FALL);
  }

  private ModBlocks() {}
}
