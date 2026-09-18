package cn.breezeth.kaleidoscope_grilling.fabric;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * First playable milestone for the Fabric 26.2 port.
 *
 * This class uses the same direct registry style as Kaleidoscope Cookery's
 * official 26.2 Fabric branch. IDs are the original Grilling IDs, so worlds
 * created with this preview can keep those IDs as the full port grows.
 */
public final class PreviewContent {
    public static final String MOD_ID = "kaleidoscope_grilling";

    public static final Block BIG_VAT = registerBlock(
            "big_vat",
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.STONE).noOcclusion());
    public static final Item BIG_VAT_ITEM = registerBlockItem("big_vat", BIG_VAT);

    public static final Item BEEF_CHUNKS = registerItem("beef_chunks");
    public static final Item CHICKEN_SKIN = registerItem("chicken_skin");
    public static final Item SQUID_TENTACLE = registerItem("squid_tentacle");
    public static final Item MINCED_HOUTTUYNIA = registerItem("minced_houttuynia");
    public static final Item CARROT_DICE = registerItem("carrot_dice");
    public static final Item POTATO_SLICE = registerItem("potato_slice");
    public static final Item RAW_MANTOU_SLICE = registerItem("raw_mantou_slice");
    public static final Item GREEN_CHILI_POWDER = registerItem("green_chili_powder");
    public static final Item SICHUAN_PEPPER = registerItem("sichuan_pepper");

    private static final ResourceKey<CreativeModeTab> MAIN_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static void init() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                MAIN_TAB,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.kaleidoscope_grilling.main"))
                        .icon(BEEF_CHUNKS::getDefaultInstance)
                        .displayItems((parameters, output) -> {
                            output.accept(BIG_VAT_ITEM);
                            output.accept(BEEF_CHUNKS);
                            output.accept(CHICKEN_SKIN);
                            output.accept(SQUID_TENTACLE);
                            output.accept(MINCED_HOUTTUYNIA);
                            output.accept(CARROT_DICE);
                            output.accept(POTATO_SLICE);
                            output.accept(RAW_MANTOU_SLICE);
                            output.accept(GREEN_CHILI_POWDER);
                            output.accept(SICHUAN_PEPPER);
                        })
                        .build());
    }

    private static Block registerBlock(String name, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(properties.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem item = new BlockItem(
                block,
                new Item.Properties().setId(key).useBlockDescriptionPrefix());
        item.registerBlocks(Item.BY_BLOCK, item);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static Item registerItem(String name) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, name));
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new Item(new Item.Properties().setId(key)));
    }

    private PreviewContent() {}
}
