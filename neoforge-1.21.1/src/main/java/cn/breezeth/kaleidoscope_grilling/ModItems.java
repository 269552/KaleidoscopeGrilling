package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, KaleidoscopeGrilling.MOD_ID);
    public static final List<DeferredHolder<Item, Item>> FIXED_SKEWERS = new ArrayList<>();
    public static final List<DeferredHolder<Item, Item>> RAW_SKEWERS = new ArrayList<>();
    public static final List<DeferredHolder<Item, Item>> INGREDIENTS = new ArrayList<>();
    public static final DeferredHolder<Item, Item> UNFINISHED_SKEWER = ITEMS.register("unfinished_skewer", () -> new UnfinishedSkewerItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> SKEWER_RECIPE_BOOK = ITEMS.register("skewer_recipe_book", () -> new SkewerRecipeBookItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SECRET_SKEWER = ITEMS.register("secret_skewer", () -> new SecretSkewerItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> BEEF_CHUNKS = ingredient("beef_chunks");
    public static final DeferredHolder<Item, Item> CHICKEN_SKIN = ingredient("chicken_skin");
    public static final DeferredHolder<Item, Item> CHICKEN_WING = ingredient("chicken_wing",()->new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.06F).build())));
    public static final DeferredHolder<Item, Item> SQUID_TENTACLE = ingredient("squid_tentacle");
    public static final DeferredHolder<Item, Item> HOUTTUYNIA = ingredient("houttuynia",()->new ItemNameBlockItem(ModBlocks.HOUTTUYNIA_CROP.get(),new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build())));
    public static final DeferredHolder<Item, Item> MINCED_HOUTTUYNIA = ingredient("minced_houttuynia");
    public static final DeferredHolder<Item, Item> CARROT_DICE = ingredient("carrot_dice");
    public static final DeferredHolder<Item, Item> RAW_SWEET_POTATO_SHEET = ingredient("raw_sweet_potato_sheet");
    public static final DeferredHolder<Item, Item> POTATO_SLICE = ingredient("potato_slice");
    public static final DeferredHolder<Item, Item> RAW_MANTOU_SLICE = ingredient("raw_mantou_slice");
    public static final DeferredHolder<Item, Item> MYSTERIOUS_SKEWER = ITEMS.register("mysterious_skewer", () -> new SkewerItem(
            new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.0F).build()),
            "tooltip.kaleidoscope_grilling.mysterious_skewer.maxim", ResourceLocation.withDefaultNamespace("nausea"), 100));
    public static final DeferredHolder<Item, Item> DARK_GRILLING = ITEMS.register("dark_grilling", () -> new SkewerItem(
            new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.0F).build()),
            "tooltip.kaleidoscope_grilling.dark_grilling.maxim", ResourceLocation.withDefaultNamespace("blindness"), 200));
    public static final DeferredHolder<Item, Item> EMPTY_SEASONING_BOTTLE = ITEMS.register("empty_seasoning_bottle", () -> new SeasoningBottleBlockItem(ModBlocks.SEASONING_BOTTLE.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PENDING_SEASONING = ITEMS.register("pending_seasoning", () -> new PendingSeasoningItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SPECIAL_SEASONING = ITEMS.register("special_seasoning", () -> new SeasoningItem(new Item.Properties().stacksTo(1).durability(16)));
    public static final DeferredHolder<Item, Item> GREEN_CHILI_POWDER = ingredient("green_chili_powder");
    public static final DeferredHolder<Item, Item> SICHUAN_PEPPER = ingredient("sichuan_pepper");
    public static final DeferredHolder<Item, Item> ONION_POWDER = ingredient("onion_powder");
    public static final DeferredHolder<Item, Item> HOUTTUYNIA_POWDER = ingredient("houttuynia_powder");
    public static final DeferredHolder<Item, Item> TOTEM_POWDER = ingredient("totem_powder");
    public static final DeferredHolder<Item, Item> DRAGON_EGG_POWDER = ingredient("dragon_egg_powder");
    public static final DeferredHolder<Item, Item> OIL_CAKE = ingredient("oil_cake");
    public static final DeferredHolder<Item, Item> OIL_RESIDUE = ingredient("oil_residue");
    public static final DeferredHolder<Item, Item> CANOLA_POWDER = ingredient("canola_powder");
    public static final DeferredHolder<Item, Item> CANOLA_FLOWER = ingredient("canola_flower");
    public static final DeferredHolder<Item, Item> MINCED_RED_CHILI = ingredient("minced_red_chili");
    public static final DeferredHolder<Item, Item> RED_CHILI_POWDER = ingredient("red_chili_powder");
    public static final DeferredHolder<Item, Item> ONION = ingredient("onion");
    public static final DeferredHolder<Item, Item> ONION_SEEDS = ITEMS.register("onion_seeds",()->new ItemNameBlockItem(ModBlocks.ONION_CROP.get(),new Item.Properties()));
    public static final DeferredHolder<Item, Item> SWEET_POTATO = ITEMS.register("sweet_potato",()->new ItemNameBlockItem(ModBlocks.SWEET_POTATO_CROP.get(),new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.1F).build())));
    public static final DeferredHolder<Item, Item> ROASTED_SWEET_POTATO = ITEMS.register("roasted_sweet_potato",()->new EffectFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.2F).build()),cookeryEffect("warmth"),600,"tooltip.kaleidoscope_grilling.roasted_sweet_potato.maxim"));
    public static final DeferredHolder<Item, Item> SWEET_POTATO_POWDER = ITEMS.register("sweet_potato_powder",()->new SweetPotatoPowderItem(new Item.Properties()));
    public static final DeferredHolder<Item, Item> ROASTED_CHICKEN_WING = ITEMS.register("roasted_chicken_wing",()->new FlavorFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.12F).build()),"tooltip.kaleidoscope_grilling.roasted_chicken_wing.maxim"));
    public static final DeferredHolder<Item, Item> COLD_HOUTTUYNIA = ITEMS.register("cold_houttuynia",()->new EffectFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()),ResourceLocation.withDefaultNamespace("fire_resistance"),1200,"tooltip.kaleidoscope_grilling.cold_houttuynia.maxim"));
    public static final DeferredHolder<Item, Item> SUGARED_TOMATO = ITEMS.register("sugared_tomato",()->new FlavorFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.65F).build()),"tooltip.kaleidoscope_grilling.sugared_tomato.maxim"));
    public static final DeferredHolder<Item, Item> CANOLA_SEEDS = ITEMS.register("canola_seeds",()->new ItemNameBlockItem(ModBlocks.CANOLA_CROP.get(),new Item.Properties()));
    public static final DeferredHolder<Item, Item> CANOLA_OIL_BUCKET = ITEMS.register("canola_oil_bucket", () -> new net.minecraft.world.item.BucketItem(ModFluids.CANOLA_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET)));
    public static final DeferredHolder<Item, Item> SECRET_CHILI_OIL_BUCKET = ITEMS.register("secret_chili_oil_bucket", () -> new net.minecraft.world.item.BucketItem(ModFluids.SECRET_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET)));
    public static final DeferredHolder<Item, Item> PREMIUM_CHILI_OIL_BUCKET = ITEMS.register("premium_chili_oil_bucket", () -> new net.minecraft.world.item.BucketItem(ModFluids.PREMIUM_SOURCE.get(), new Item.Properties().stacksTo(1).craftRemainder(net.minecraft.world.item.Items.BUCKET)));

    static {
        skewer("grilled_beef_skewer", 5, 0.6F, cookeryEffect("vigor"), 15);
        skewer("grilled_pork_belly_skewer", 5, 0.6F, cookeryEffect("vigor"), 15);
        skewer("grilled_chicken_skin_skewer", 4, 0.5F, cookeryEffect("vigor"), 10);
        skewer("grilled_mid_wing_skewer", 7, 0.2143F, cookeryEffect("vigor"), 20);
        skewer("grilled_squid_tentacle_skewer", 5, 0.4F, ResourceLocation.withDefaultNamespace("water_breathing"), 30);
        skewer("grilled_fish_skewer", 6, 0.45F, cookeryEffect("warmth"), 10);
        skewer("grilled_sweet_potato_sheet_skewer", 6, 1.1667F, cookeryEffect("warmth"), 20);
        skewer("grilled_potato_slice_skewer", 6, 0.1667F, cookeryEffect("warmth"), 20);
        skewer("grilled_caterpillar_skewer", 12, 0.0833F, cookeryEffect("flatulence"), 5);
        skewer("grilled_mushroom_skewer", 5, 0.55F, cookeryEffect("warmth"), 20);
        skewer("grilled_bun_slice_skewer", 5, 0.6F, null, 0);
        skewer("grilled_ender_pearl_skewer", 4, 0.1F, cookeryEffect("flatulence"), 20);
        skewer("grilled_meatball_skewer", 8, 0.875F, cookeryEffect("warmth"), 60);
        skewer("grilled_slime_skewer", 4, 0.0F, cookeryEffect("flatulence"), 10);
        skewer("grilled_meat_and_bone_skewer", 4, 0.5F, cookeryEffect("vigor"), 20);
        skewer("grilled_fried_egg_skewer", 4, 0.375F, cookeryEffect("warmth"), 10);

        rawSkewer("raw_beef_skewer", 2, 0.3F, false);
        rawSkewer("raw_pork_belly_skewer", 2, 0.3F, false);
        rawSkewer("raw_chicken_skin_skewer", 2, 0.25F, false);
        rawSkewer("raw_mid_wing_skewer", 3, 0.1071F, false);
        rawSkewer("raw_squid_tentacle_skewer", 2, 0.2F, false);
        rawSkewer("raw_fish_skewer", 3, 0.225F, false);
        rawSkewer("raw_sweet_potato_sheet_skewer", 3, 0.5833F, false);
        rawSkewer("raw_potato_slice_skewer", 3, 0.0833F, false);
        rawSkewer("raw_caterpillar_skewer", 6, 0.0417F, true);
        rawSkewer("raw_mushroom_skewer", 2, 0.275F, false);
        rawSkewer("raw_bun_slice_skewer", 2, 0.3F, false);
        rawSkewer("raw_ender_pearl_skewer", 2, 0.05F, true);
        rawSkewer("raw_meatball_skewer", 4, 0.4375F, false);
        rawSkewer("raw_slime_skewer", 2, 0.0F, true);
        rawSkewer("raw_meat_and_bone_skewer", 2, 0.25F, false);
        rawSkewer("raw_fried_egg_skewer", 2, 0.1875F, false);
    }

    private static ResourceLocation cookeryEffect(String path) {
        return ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", path);
    }

    private static DeferredHolder<Item, Item> ingredient(String id) {
        return ingredient(id,()->new Item(new Item.Properties()));
    }

    private static DeferredHolder<Item, Item> ingredient(String id, Supplier<? extends Item> factory) {
        DeferredHolder<Item, Item> item = ITEMS.register(id, factory);
        INGREDIENTS.add(item);
        return item;
    }

    private static void skewer(String id, int nutrition, float saturationModifier,
                               ResourceLocation effectId, int effectSeconds) {
        FIXED_SKEWERS.add(ITEMS.register(id, () -> new SkewerItem(new Item.Properties().food(
                new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build()),
                "tooltip.kaleidoscope_grilling." + id + ".maxim", effectId, effectSeconds * 20)));
    }

    private static void rawSkewer(String id, int nutrition, float saturationModifier, boolean nausea) {
        FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier);
        RAW_SKEWERS.add(ITEMS.register(id, () -> new SkewerItem(new Item.Properties().food(food.build()), null,
                nausea ? ResourceLocation.withDefaultNamespace("nausea") : null, nausea ? 60 : 0)));
    }

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            FIXED_SKEWERS.forEach(item -> event.accept(item.get()));
            RAW_SKEWERS.forEach(item -> event.accept(item.get()));
            INGREDIENTS.forEach(item -> event.accept(item.get()));
            event.accept(UNFINISHED_SKEWER.get());
            event.accept(SKEWER_RECIPE_BOOK.get());
            event.accept(SECRET_SKEWER.get());
            event.accept(ModBlocks.GRILL_ITEM.get());
            event.accept(ModBlocks.BIG_VAT_ITEM.get());
            event.accept(ModBlocks.OIL_PRESS_ITEM.get());
            event.accept(MYSTERIOUS_SKEWER.get());
            event.accept(DARK_GRILLING.get());
            event.accept(EMPTY_SEASONING_BOTTLE.get());
            event.accept(PENDING_SEASONING.get());
            event.accept(SPECIAL_SEASONING.get());
            event.accept(CANOLA_OIL_BUCKET.get()); event.accept(SECRET_CHILI_OIL_BUCKET.get()); event.accept(PREMIUM_CHILI_OIL_BUCKET.get());
            event.accept(CANOLA_SEEDS.get());
            event.accept(ONION_SEEDS.get());
            event.accept(SWEET_POTATO.get());
            event.accept(ROASTED_SWEET_POTATO.get());
        }
    }

    private ModItems() {}
}
