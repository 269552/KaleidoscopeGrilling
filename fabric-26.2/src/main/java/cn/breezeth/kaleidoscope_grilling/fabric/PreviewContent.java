package cn.breezeth.kaleidoscope_grilling.fabric;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Functional preview content for the Fabric 26.2 port.
 *
 * Preview 3 restores real food components, a first batch of vanilla status
 * effects, placeable equipment/decorative blocks and the original registry IDs.
 * More complex block entities, fluids, grilling state machines and custom
 * Grilling/Cookery effects are still being ported separately.
 */
public final class PreviewContent {
    public static final String MOD_ID = "kaleidoscope_grilling";

    private record FoodSpec(
            int nutrition,
            float saturation,
            boolean alwaysEdible,
            Holder<MobEffect> effect,
            int effectTicks) {}

    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();
    private static final Map<String, FoodSpec> FOODS = new LinkedHashMap<>();

    private static final Set<String> PLACEABLE_BLOCK_ITEMS = Set.of(
            "advanced_rack",
            "grill",
            "oil_press",
            "pepper_log",
            "pepper_leaves",
            "pepper_sapling",
            "skewer_plate");

    private static final Set<String> SEASONING_BOTTLE_ITEMS = Set.of(
            "empty_seasoning_bottle", "pending_seasoning", "special_seasoning");

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

    static {
        defineFoods();
        registerBlocks();

        // Big Vat is a real block too, but it is not present in ITEM_IDS.
        ITEMS.put("big_vat", registerBlockItem("big_vat", BLOCKS.get("big_vat"), baseProperties("big_vat"), true));

        for (String id : ITEM_IDS) {
            ITEMS.put(id, registerPreviewItem(id));
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
                        .displayItems((parameters, output) -> ITEMS.values().forEach(output::accept))
                        .build());
    }

    private static void registerBlocks() {
        block("big_vat", BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.STONE).noOcclusion());
        block("grill", BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL).noOcclusion());
        block("oil_press", BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD).noOcclusion());
        block("advanced_rack", BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD).noOcclusion());
        block("pepper_log", BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.WOOD));
        block("pepper_leaves", BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.GRASS).noOcclusion());
        block("pepper_sapling", BlockBehaviour.Properties.of().strength(0.0F).sound(SoundType.GRASS).noOcclusion().noCollission());
        block("skewer_plate", BlockBehaviour.Properties.of().strength(0.2F).sound(SoundType.WOOD).noOcclusion());
        block("seasoning_bottle", BlockBehaviour.Properties.of().strength(0.0F).sound(SoundType.GLASS).noOcclusion());
    }

    private static void defineFoods() {
        // Standalone dishes and ingredients from the 1.21.1 source.
        food("secret_skewer", 1, 0.0F);
        food("skewer_plate", 1, 0.0F);
        food("chicken_wing", 2, 0.06F);
        food("houttuynia", 2, 0.2F);
        food("mysterious_skewer", 2, 0.0F, false, MobEffects.NAUSEA, 100);
        food("dark_grilling", 1, 0.0F, false, MobEffects.BLINDNESS, 200);
        food("sweet_potato", 3, 0.1F);
        food("roasted_sweet_potato", 6, 0.2F);
        food("roasted_chicken_wing", 5, 0.12F);
        food("cold_houttuynia", 6, 1.0F, false, MobEffects.FIRE_RESISTANCE, 1200);
        food("sugared_tomato", 6, 0.65F);
        food("pepper_honey", 4, 0.25F);
        food("wedding_candy", 20, 0.5F, true, null, 0);
        food("houttuynia_stir_fried_pork", 9, 0.7F);
        food("green_pepper_squid_tentacles", 8, 0.6F);
        food("braised_chicken_wings", 10, 0.8F);
        food("potato_beef_stew", 12, 0.9F);
        food("red_sweet_potato_porridge", 14, 0.071429F);
        food("sour_spicy_noodles", 10, 0.6F);
        food("ordinary_skewer", 5, 0.46F, true, null, 0);

        // Raw skewers.
        food("raw_beef_skewer", 2, 0.3F);
        food("raw_pork_belly_skewer", 2, 0.3F);
        food("raw_chicken_skin_skewer", 2, 0.25F);
        food("raw_mid_wing_skewer", 3, 0.1071F);
        food("raw_squid_tentacle_skewer", 2, 0.2F);
        food("raw_fish_skewer", 3, 0.225F);
        food("raw_sweet_potato_sheet_skewer", 3, 0.5833F);
        food("raw_potato_slice_skewer", 3, 0.0833F);
        food("raw_caterpillar_skewer", 6, 0.0417F, false, MobEffects.NAUSEA, 60);
        food("raw_mushroom_skewer", 2, 0.275F);
        food("raw_bun_slice_skewer", 2, 0.3F);
        food("raw_ender_pearl_skewer", 2, 0.05F, false, MobEffects.NAUSEA, 60);
        food("raw_meatball_skewer", 4, 0.4375F);
        food("raw_slime_skewer", 2, 0.0F, false, MobEffects.NAUSEA, 60);
        food("raw_meat_and_bone_skewer", 2, 0.25F);
        food("raw_fried_egg_skewer", 2, 0.1875F);
        food("raw_gluten_skewer", 3, 0.05F);
        food("raw_lamb_skewer", 4, 0.8F);
        food("raw_golden_skewer", 6, 1.2F);

        // Grilled skewers. Vanilla effects are restored here; Cookery-specific
        // effects such as warmth/vigor are handled in a later port stage.
        food("grilled_beef_skewer", 5, 0.6F, false, MobEffects.STRENGTH, 10 * 20);
        food("grilled_pork_belly_skewer", 5, 0.6F);
        food("grilled_chicken_skin_skewer", 4, 0.5F, false, MobEffects.SPEED, 20 * 20);
        food("grilled_mid_wing_skewer", 7, 0.2143F);
        food("grilled_squid_tentacle_skewer", 5, 0.4F, false, MobEffects.WATER_BREATHING, 30 * 20);
        food("grilled_fish_skewer", 6, 0.45F);
        food("grilled_sweet_potato_sheet_skewer", 6, 1.1667F);
        food("grilled_potato_slice_skewer", 6, 0.1667F);
        food("grilled_caterpillar_skewer", 12, 0.0833F);
        food("grilled_mushroom_skewer", 5, 0.55F, false, MobEffects.NIGHT_VISION, 30 * 20);
        food("grilled_bun_slice_skewer", 5, 0.6F);
        food("grilled_ender_pearl_skewer", 4, 0.1F);
        food("grilled_meatball_skewer", 12, 0.875F, false, MobEffects.STRENGTH, 10 * 20);
        food("grilled_slime_skewer", 4, 0.0F);
        food("grilled_meat_and_bone_skewer", 4, 0.5F);
        food("grilled_fried_egg_skewer", 4, 0.375F);
        food("grilled_gluten_skewer", 6, 0.1F, false, MobEffects.HASTE, 30 * 20);
        food("grilled_lamb_skewer", 8, 0.8F);
        food("grilled_golden_skewer", 12, 1.2F);
    }

    private static void food(String id, int nutrition, float saturation) {
        food(id, nutrition, saturation, false, null, 0);
    }

    private static void food(
            String id,
            int nutrition,
            float saturation,
            boolean alwaysEdible,
            Holder<MobEffect> effect,
            int effectTicks) {
        FOODS.put(id, new FoodSpec(nutrition, saturation, alwaysEdible, effect, effectTicks));
    }

    private static void block(String name, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(properties.setId(key));
        BLOCKS.put(name, Registry.register(BuiltInRegistries.BLOCK, key, block));
    }

    private static Item registerPreviewItem(String name) {
        Item.Properties properties = baseProperties(name);

        FoodSpec food = FOODS.get(name);
        if (food != null) {
            FoodProperties.Builder builder = new FoodProperties.Builder()
                    .nutrition(food.nutrition())
                    .saturationModifier(food.saturation());
            if (food.alwaysEdible()) {
                builder.alwaysEdible();
            }

            if (food.effect() != null) {
                Consumable consumable = Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(
                                new MobEffectInstance(food.effect(), food.effectTicks(), 0), 1.0F))
                        .build();
                properties.food(builder.build(), consumable);
            } else {
                properties.food(builder.build());
            }
        }

        if (PLACEABLE_BLOCK_ITEMS.contains(name)) {
            return registerBlockItem(name, BLOCKS.get(name), properties, true);
        }

        if (SEASONING_BOTTLE_ITEMS.contains(name)) {
            return registerBlockItem(
                    name,
                    BLOCKS.get("seasoning_bottle"),
                    properties.stacksTo(1),
                    name.equals("empty_seasoning_bottle"));
        }

        ResourceKey<Item> key = itemKey(name);
        return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
    }

    private static Item.Properties baseProperties(String name) {
        Item.Properties properties = new Item.Properties().setId(itemKey(name));
        if (name.endsWith("_oil_brush")
                || name.endsWith("_oil_bucket")
                || name.equals("skewer_recipe_book")
                || name.equals("skewer_plate")) {
            properties.stacksTo(1);
        } else if (name.equals("red_sweet_potato_porridge")
                || name.equals("sour_spicy_noodles")
                || name.equals("houttuynia_stir_fried_pork")
                || name.equals("green_pepper_squid_tentacles")
                || name.equals("braised_chicken_wings")
                || name.equals("potato_beef_stew")) {
            properties.stacksTo(16);
        }
        return properties;
    }

    private static Item registerBlockItem(
            String name, Block block, Item.Properties properties, boolean registerAsBlockItem) {
        if (block == null) {
            ResourceKey<Item> key = itemKey(name);
            return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties));
        }
        ResourceKey<Item> key = itemKey(name);
        BlockItem item = new BlockItem(block, properties.useBlockDescriptionPrefix());
        if (registerAsBlockItem) {
            item.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    private PreviewContent() {}
}
