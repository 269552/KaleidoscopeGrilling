package cn.breezeth.kaleidoscope_grilling.fabric;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
 * Expanded playable-preview content for the Fabric 26.2 port.
 *
 * Preview 2 exposes the original top-level item catalogue in the creative tab
 * while the original NeoForge behaviours are ported incrementally. Registry IDs
 * and artwork stay compatible with the upstream Grilling resources.
 */
public final class PreviewContent {
    public static final String MOD_ID = "kaleidoscope_grilling";

    public static final Block BIG_VAT = registerBlock(
            "big_vat",
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.STONE).noOcclusion());
    public static final Item BIG_VAT_ITEM = registerBlockItem("big_vat", BIG_VAT);

    private static final Set<String> BLOCK_LIKE_ITEMS = Set.of(
            "advanced_rack", "grill", "oil_press", "pepper_log", "pepper_leaves", "pepper_sapling");

    private static final String[] ITEM_IDS = {
            "advanced_rack", "beef_chunks", "braised_chicken_wings", "canola_oil_brush", "canola_oil_bucket", "canola_powder",
            "canola_seeds", "carrot_dice", "chicken_skin", "chicken_wing", "cold_houttuynia", "dark_grilling",
            "dragon_egg_powder", "empty_seasoning_bottle", "green_chili_powder", "green_pepper_squid_tentacles", "grill", "grilled_beef_skewer",
            "grilled_bun_slice_skewer", "grilled_caterpillar_skewer", "grilled_chicken_skin_skewer", "grilled_ender_pearl_skewer", "grilled_fish_skewer", "grilled_fried_egg_skewer",
            "grilled_gluten_skewer", "grilled_golden_skewer", "grilled_lamb_skewer", "grilled_meat_and_bone_skewer", "grilled_meatball_skewer", "grilled_mid_wing_skewer",
            "grilled_mushroom_skewer", "grilled_pork_belly_skewer", "grilled_potato_slice_skewer", "grilled_slime_skewer", "grilled_squid_tentacle_skewer", "grilled_sweet_potato_sheet_skewer",
            "houttuynia", "houttuynia_powder", "houttuynia_stir_fried_pork", "minced_houttuynia", "mysterious_skewer", "oil_cake",
            "oil_residue", "onion", "onion_powder", "ordinary_skewer", "pending_seasoning", "pepper_honey",
            "pepper_leaves", "pepper_log", "pepper_sapling", "potato_beef_stew", "potato_slice", "premium_chili_oil_brush",
            "premium_chili_oil_bucket", "raw_beef_skewer", "raw_bun_slice_skewer", "raw_caterpillar_skewer", "raw_chicken_skin_skewer", "raw_ender_pearl_skewer",
            "raw_fish_skewer", "raw_fried_egg_skewer", "raw_gluten_skewer", "raw_golden_skewer", "raw_lamb_skewer", "raw_mantou_slice",
            "raw_meat_and_bone_skewer", "raw_meatball_skewer", "raw_mid_wing_skewer", "raw_mushroom_skewer", "raw_pork_belly_skewer", "raw_potato_slice_skewer",
            "raw_slime_skewer", "raw_squid_tentacle_skewer", "raw_sweet_potato_sheet", "raw_sweet_potato_sheet_skewer", "red_chili_powder", "red_sweet_potato_porridge",
            "roasted_chicken_wing", "roasted_sweet_potato", "secret_chili_oil_brush", "secret_chili_oil_bucket", "secret_skewer", "sichuan_pepper",
            "skewer_plate", "skewer_recipe_book", "sour_spicy_noodles", "special_seasoning", "squid_tentacle", "sugared_tomato",
            "sweet_potato", "sweet_potato_powder", "totem_powder", "unfinished_skewer", "wedding_candy"
    };

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    static {
        for (String id : ITEM_IDS) {
            ITEMS.put(id, registerPreviewItem(id, BLOCK_LIKE_ITEMS.contains(id)));
        }
    }

    private static final ResourceKey<CreativeModeTab> MAIN_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static void init() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                MAIN_TAB,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.kaleidoscope_grilling.main"))
                        .icon(() -> ITEMS.get("grill").getDefaultInstance())
                        .displayItems((parameters, output) -> {
                            output.accept(BIG_VAT_ITEM);
                            ITEMS.values().forEach(output::accept);
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

    private static Item registerPreviewItem(String name, boolean useBlockDescriptionPrefix) {
        ResourceKey<Item> key = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item.Properties properties = new Item.Properties().setId(key);
        if (useBlockDescriptionPrefix) {
            properties = properties.useBlockDescriptionPrefix();
        }
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
    }

    private PreviewContent() {}
}
