package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private static final SoundType COOKERY_POT_SOUND = new SoundType(1.0F, 0.8F,
            SoundEvents.LANTERN_BREAK, SoundEvents.LANTERN_STEP, SoundEvents.LANTERN_PLACE,
            SoundEvents.LANTERN_HIT, SoundEvents.LANTERN_FALL);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KaleidoscopeGrilling.MOD_ID);
    public static final RegistryObject<Block> GRILL = BLOCKS.register("grill", () -> new GrillBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS).sound(COOKERY_POT_SOUND).strength(3.0F).lightLevel(s->s.getValue(GrillBlock.LIT)?7:0).noOcclusion()));
    public static final RegistryObject<Block> SEASONING_BOTTLE = BLOCKS.register("seasoning_bottle", () -> new SeasoningBottleBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS).sound(seasoningBottleSound()).instabreak().noOcclusion()));
    public static final RegistryObject<Block> BIG_VAT = BLOCKS.register("big_vat", () -> new BigVatBlock(
            BlockBehaviour.Properties.copy(Blocks.BRICKS).strength(2.0F).noOcclusion()));
    public static final RegistryObject<Block> OIL_PRESS = BLOCKS.register("oil_press", () -> new OilPressBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion()));
    public static final RegistryObject<Block> ADVANCED_RACK = BLOCKS.register("advanced_rack",()->new AdvancedRackBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB).strength(2.0F).noOcclusion()));
    public static final RegistryObject<Block> SKEWER_RECIPE = BLOCKS.register("skewer_recipe", () -> new SkewerRecipeBlock(
            BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).instabreak().noOcclusion()));
    public static final RegistryObject<Block> CANOLA_CROP = BLOCKS.register("canola_crop", () -> new CanolaCropBlock(
            BlockBehaviour.Properties.copy(Blocks.WHEAT).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final RegistryObject<Block> ONION_CROP = BLOCKS.register("onion_crop", () -> new OnionCropBlock(
            BlockBehaviour.Properties.copy(Blocks.WHEAT).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final RegistryObject<Block> SWEET_POTATO_CROP = BLOCKS.register("sweet_potato_crop", () -> new SweetPotatoCropBlock(
            BlockBehaviour.Properties.copy(Blocks.BEETROOTS).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final RegistryObject<Block> HOUTTUYNIA_CROP = BLOCKS.register("houttuynia_crop", () -> new HouttuyniaCropBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHER_WART).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final RegistryObject<Block> PEPPER_LOG = BLOCKS.register("pepper_log", PepperLogBlock::new);
    public static final RegistryObject<Block> PEPPER_LEAVES = BLOCKS.register("pepper_leaves", PepperLeavesBlock::new);
    public static final RegistryObject<Block> PEPPER_SAPLING = BLOCKS.register("pepper_sapling", PepperSaplingBlock::new);
    public static final RegistryObject<Item> PEPPER_LOG_ITEM = ModItems.ITEMS.register("pepper_log",
            () -> new BlockItem(PEPPER_LOG.get(), new Item.Properties()));
    public static final RegistryObject<Item> PEPPER_LEAVES_ITEM = ModItems.ITEMS.register("pepper_leaves",
            () -> new BlockItem(PEPPER_LEAVES.get(), new Item.Properties()));
    public static final RegistryObject<Item> PEPPER_SAPLING_ITEM = ModItems.ITEMS.register("pepper_sapling",
            () -> new BlockItem(PEPPER_SAPLING.get(), new Item.Properties()));
    public static final RegistryObject<Item> GRILL_ITEM = ModItems.ITEMS.register("grill",
            () -> new FunctionalBlockItem(GRILL.get(), new Item.Properties(),
                    "tooltip.kaleidoscope_grilling.grill.usage", null));
    public static final RegistryObject<Item> BIG_VAT_ITEM = ModItems.ITEMS.register("big_vat",
            () -> new FunctionalBlockItem(BIG_VAT.get(), new Item.Properties(),
                    "tooltip.kaleidoscope_grilling.big_vat.usage",
                    "tooltip.kaleidoscope_grilling.big_vat.requirement"));
    public static final RegistryObject<Item> OIL_PRESS_ITEM = ModItems.ITEMS.register("oil_press",
            () -> new FunctionalBlockItem(OIL_PRESS.get(), new Item.Properties(),
                    "tooltip.kaleidoscope_grilling.oil_press.usage",
                    "tooltip.kaleidoscope_grilling.oil_press.requirement"));
    public static final RegistryObject<Item> ADVANCED_RACK_ITEM=ModItems.ITEMS.register("advanced_rack",()->new BlockItem(ADVANCED_RACK.get(),new Item.Properties()));
    private static SoundType seasoningBottleSound() {
        return new SoundType(1.0F, 1.0F, ModSounds.BOTTLE_INTERACT.get(), SoundEvents.GLASS_STEP,
                ModSounds.BOTTLE_INTERACT.get(), ModSounds.BOTTLE_INTERACT.get(), SoundEvents.GLASS_FALL);
    }
    private ModBlocks() {}
}
