package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;
import cn.breezeth.kaleidoscope_grilling.grill.GrillBlock;
import cn.breezeth.kaleidoscope_grilling.item.FunctionalBlockItem;
import cn.breezeth.kaleidoscope_grilling.oil.BigVatBlock;
import cn.breezeth.kaleidoscope_grilling.oil.OilPressBlock;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackBlock;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackBlockItem;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningBottleBlock;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerPlateBlock;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipeBlock;
import cn.breezeth.kaleidoscope_grilling.world.CanolaCropBlock;
import cn.breezeth.kaleidoscope_grilling.world.HouttuyniaCropBlock;
import cn.breezeth.kaleidoscope_grilling.world.OnionCropBlock;
import cn.breezeth.kaleidoscope_grilling.world.PepperLeavesBlock;
import cn.breezeth.kaleidoscope_grilling.world.PepperLogBlock;
import cn.breezeth.kaleidoscope_grilling.world.PepperSaplingBlock;
import cn.breezeth.kaleidoscope_grilling.world.SweetPotatoCropBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

  public static final RegistryRef<Block> GRILL =
      block(
          "grill",
          GrillBlock::new,
          BlockBehaviour.Properties.ofLegacyCopy(Blocks.IRON_BARS)
              .sound(COOKERY_POT_SOUND)
              .strength(3.0F)
              .lightLevel(s -> s.getValue(GrillBlock.LIT) ? 7 : 0)
              .noOcclusion());
  public static final RegistryRef<Block> SEASONING_BOTTLE =
      block(
          "seasoning_bottle",
          SeasoningBottleBlock::new,
          BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
              .sound(seasoningBottleSound())
              .instabreak()
              .noOcclusion());
  public static final RegistryRef<Block> BIG_VAT =
      block(
          "big_vat",
          BigVatBlock::new,
          BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).strength(2.0F).noOcclusion());
  public static final RegistryRef<Block> OIL_PRESS =
      block(
          "oil_press",
          OilPressBlock::new,
          BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion());
  public static final RegistryRef<Block> ADVANCED_RACK =
      block(
          "advanced_rack",
          AdvancedRackBlock::new,
          BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).strength(2.0F).noOcclusion());
  public static final RegistryRef<Block> SKEWER_RECIPE =
      block(
          "skewer_recipe",
          SkewerRecipeBlock::new,
          BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).instabreak().noOcclusion());
  public static final RegistryRef<Block> SKEWER_PLATE =
      block(
          "skewer_plate",
          SkewerPlateBlock::new,
          BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE).instabreak().noOcclusion());
  public static final RegistryRef<Block> CANOLA_CROP =
      block(
          "canola_crop",
          CanolaCropBlock::new,
          BlockBehaviour.Properties.ofLegacyCopy(Blocks.WHEAT)
              .noCollission().randomTicks().instabreak().noOcclusion());
  public static final RegistryRef<Block> ONION_CROP =
      block(
          "onion_crop",
          OnionCropBlock::new,
          BlockBehaviour.Properties.ofLegacyCopy(Blocks.WHEAT)
              .noCollission().randomTicks().instabreak().noOcclusion());
  public static final RegistryRef<Block> SWEET_POTATO_CROP =
      block(
          "sweet_potato_crop",
          SweetPotatoCropBlock::new,
          BlockBehaviour.Properties.ofLegacyCopy(Blocks.BEETROOTS)
              .noCollission().randomTicks().instabreak().noOcclusion());
  public static final RegistryRef<Block> HOUTTUYNIA_CROP =
      block(
          "houttuynia_crop",
          HouttuyniaCropBlock::new,
          BlockBehaviour.Properties.ofLegacyCopy(Blocks.NETHER_WART)
              .noCollission().randomTicks().instabreak().noOcclusion());
  public static final RegistryRef<Block> PEPPER_LOG = block("pepper_log", p -> PepperLogBlock.create(p), BlockBehaviour.Properties.of());
  public static final RegistryRef<Block> PEPPER_LEAVES = block("pepper_leaves", p -> PepperLeavesBlock.create(p), BlockBehaviour.Properties.of());
  public static final RegistryRef<Block> PEPPER_SAPLING = block("pepper_sapling", p -> PepperSaplingBlock.create(p), BlockBehaviour.Properties.of());

  public static final RegistryRef<Item> PEPPER_LOG_ITEM =
      item("pepper_log", p -> new BlockItem(PEPPER_LOG.get(), p.useBlockDescriptionPrefix()));
  public static final RegistryRef<Item> PEPPER_LEAVES_ITEM =
      item("pepper_leaves", p -> new BlockItem(PEPPER_LEAVES.get(), p.useBlockDescriptionPrefix()));
  public static final RegistryRef<Item> PEPPER_SAPLING_ITEM =
      item("pepper_sapling", p -> new BlockItem(PEPPER_SAPLING.get(), p.useBlockDescriptionPrefix()));
  public static final RegistryRef<Item> GRILL_ITEM =
      item(
          "grill",
          p -> new FunctionalBlockItem(
              GRILL.get(), p.useBlockDescriptionPrefix(), "tooltip.kaleidoscope_grilling.grill.usage", null));
  public static final RegistryRef<Item> BIG_VAT_ITEM =
      item(
          "big_vat",
          p -> new FunctionalBlockItem(
              BIG_VAT.get(), p.useBlockDescriptionPrefix(),
              "tooltip.kaleidoscope_grilling.big_vat.usage",
              "tooltip.kaleidoscope_grilling.big_vat.requirement"));
  public static final RegistryRef<Item> OIL_PRESS_ITEM =
      item(
          "oil_press",
          p -> new FunctionalBlockItem(
              OIL_PRESS.get(), p.useBlockDescriptionPrefix(),
              "tooltip.kaleidoscope_grilling.oil_press.usage",
              "tooltip.kaleidoscope_grilling.oil_press.requirement"));
  public static final RegistryRef<Item> ADVANCED_RACK_ITEM =
      item("advanced_rack", p -> new AdvancedRackBlockItem(ADVANCED_RACK.get(), p.useBlockDescriptionPrefix()));

  private static RegistryRef<Block> block(
      String name,
      Function<BlockBehaviour.Properties, ? extends Block> factory,
      BlockBehaviour.Properties properties) {
    ResourceKey<Block> key = ResourceKey.create(
        Registries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name));
    Block value = factory.apply(properties.setId(key));
    return RegistryRef.of(Registry.register(BuiltInRegistries.BLOCK, key, value));
  }

  private static RegistryRef<Item> item(String name, Function<Item.Properties, ? extends Item> factory) {
    ResourceKey<Item> key = ResourceKey.create(
        Registries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name));
    Item value = factory.apply(new Item.Properties().setId(key));
    if (value instanceof BlockItem blockItem) blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
    return RegistryRef.of(Registry.register(BuiltInRegistries.ITEM, key, value));
  }

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

  public static void init() {}

  private ModBlocks() {}
}
