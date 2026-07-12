package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, KaleidoscopeGrilling.MOD_ID);
    public static final DeferredHolder<Block, Block> GRILL = BLOCKS.register("grill", () -> new GrillBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).strength(3.0F).noOcclusion()));
    public static final DeferredHolder<Block, Block> SEASONING_BOTTLE = BLOCKS.register("seasoning_bottle", () -> new SeasoningBottleBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.5F).noOcclusion()));
    public static final DeferredHolder<Block, Block> BIG_VAT = BLOCKS.register("big_vat", () -> new BigVatBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).strength(2.0F).noOcclusion()));
    public static final DeferredHolder<Block, Block> OIL_PRESS = BLOCKS.register("oil_press", () -> new OilPressBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion()));
    public static final DeferredHolder<Block, Block> CANOLA_CROP = BLOCKS.register("canola_crop", () -> new CanolaCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final DeferredHolder<Block, Block> ONION_CROP = BLOCKS.register("onion_crop", () -> new OnionCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final DeferredHolder<Block, Block> SWEET_POTATO_CROP = BLOCKS.register("sweet_potato_crop", () -> new SweetPotatoCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BEETROOTS).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final DeferredHolder<Block, Block> HOUTTUYNIA_CROP = BLOCKS.register("houttuynia_crop", () -> new HouttuyniaCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_WART).noCollission().randomTicks().instabreak().noOcclusion()));
    public static final DeferredHolder<Block, Block> PEPPER_LOG = BLOCKS.register("pepper_log", PepperLogBlock::create);
    public static final DeferredHolder<Block, Block> PEPPER_LEAVES = BLOCKS.register("pepper_leaves", PepperLeavesBlock::create);
    public static final DeferredHolder<Block, Block> PEPPER_SAPLING = BLOCKS.register("pepper_sapling", PepperSaplingBlock::create);
    public static final DeferredHolder<Item, Item> PEPPER_LOG_ITEM = ModItems.ITEMS.register("pepper_log",
            () -> new BlockItem(PEPPER_LOG.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEPPER_LEAVES_ITEM = ModItems.ITEMS.register("pepper_leaves",
            () -> new BlockItem(PEPPER_LEAVES.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> PEPPER_SAPLING_ITEM = ModItems.ITEMS.register("pepper_sapling",
            () -> new BlockItem(PEPPER_SAPLING.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> GRILL_ITEM = ModItems.ITEMS.register("grill",
            () -> new BlockItem(GRILL.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BIG_VAT_ITEM = ModItems.ITEMS.register("big_vat",
            () -> new BlockItem(BIG_VAT.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> OIL_PRESS_ITEM = ModItems.ITEMS.register("oil_press", () -> new BlockItem(OIL_PRESS.get(),new Item.Properties()));
    private ModBlocks() {}
}
