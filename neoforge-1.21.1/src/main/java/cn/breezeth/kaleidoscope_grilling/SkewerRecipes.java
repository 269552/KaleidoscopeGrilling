package cn.breezeth.kaleidoscope_grilling;

import java.util.List;

public final class SkewerRecipes {
    private record Recipe(String result, List<List<String>> ingredients) {}
    private static List<String> any(String... ids) { return List.of(ids); }
    private static final List<Recipe> RECIPES = List.of(
            recipe("raw_beef_skewer", any("kaleidoscope_grilling:beef_chunks"), any("kaleidoscope_cookery:red_chili"), any("kaleidoscope_grilling:beef_chunks")),
            recipe("raw_pork_belly_skewer", any("kaleidoscope_cookery:raw_pork_belly"), any("kaleidoscope_cookery:green_chili"), any("kaleidoscope_cookery:raw_pork_belly")),
            recipe("raw_chicken_skin_skewer", any("kaleidoscope_grilling:chicken_skin"), any("kaleidoscope_grilling:chicken_skin")),
            recipe("raw_mid_wing_skewer", any("kaleidoscope_grilling:chicken_wing"), any("kaleidoscope_cookery:red_chili"), any("kaleidoscope_grilling:chicken_wing")),
            recipe("raw_squid_tentacle_skewer", any("kaleidoscope_grilling:squid_tentacle"), any("kaleidoscope_grilling:squid_tentacle"), any("kaleidoscope_grilling:squid_tentacle")),
            recipe("raw_fish_skewer", any("minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish", "minecraft:pufferfish")),
            recipe("raw_sweet_potato_sheet_skewer", any("kaleidoscope_grilling:raw_sweet_potato_sheet"), any("kaleidoscope_grilling:minced_houttuynia"), any("kaleidoscope_grilling:minced_houttuynia")),
            recipe("raw_potato_slice_skewer", any("kaleidoscope_grilling:potato_slice"), any("kaleidoscope_grilling:potato_slice"), any("kaleidoscope_grilling:potato_slice")),
            recipe("raw_caterpillar_skewer", any("kaleidoscope_cookery:caterpillar")),
            recipe("raw_mushroom_skewer", any("minecraft:brown_mushroom", "minecraft:red_mushroom"), any("kaleidoscope_grilling:carrot_dice"), any("minecraft:brown_mushroom", "minecraft:red_mushroom")),
            recipe("raw_bun_slice_skewer", any("kaleidoscope_grilling:raw_mantou_slice"), any("kaleidoscope_grilling:raw_mantou_slice"), any("kaleidoscope_grilling:raw_mantou_slice")),
            recipe("raw_ender_pearl_skewer", any("minecraft:ender_pearl"), any("minecraft:beetroot"), any("minecraft:ender_pearl")),
            recipe("raw_meatball_skewer", any("kaleidoscope_cookery:raw_meatball"), any("kaleidoscope_cookery:raw_meatball"), any("kaleidoscope_cookery:raw_meatball")),
            recipe("raw_slime_skewer", any("minecraft:slime_ball"), any("kaleidoscope_grilling:houttuynia"), any("minecraft:slime_ball")),
            recipe("raw_meat_and_bone_skewer", any("kaleidoscope_cookery:raw_cut_small_meats"), any("minecraft:bone"), any("kaleidoscope_cookery:raw_cut_small_meats")),
            recipe("raw_fried_egg_skewer", any("kaleidoscope_cookery:fried_egg"), any("kaleidoscope_cookery:fried_egg"))
    );

    private static Recipe recipe(String result, List<String>... ingredients) { return new Recipe(result, List.of(ingredients)); }
    public static boolean canAppend(List<String> inserted, String next) { int index = inserted.size(); return RECIPES.stream().anyMatch(recipe -> index < recipe.ingredients.size() && matchesPrefix(recipe, inserted) && recipe.ingredients.get(index).contains(next)); }
    public static String completedResult(List<String> inserted) { return RECIPES.stream().filter(recipe -> recipe.ingredients.size() == inserted.size() && matchesPrefix(recipe, inserted)).map(Recipe::result).findFirst().orElse(null); }
    public static int expectedSize(List<String> inserted) { return RECIPES.stream().filter(recipe -> matchesPrefix(recipe, inserted)).mapToInt(recipe -> recipe.ingredients.size()).max().orElse(3); }
    private static boolean matchesPrefix(Recipe recipe, List<String> inserted) { for (int i = 0; i < inserted.size(); i++) if (!recipe.ingredients.get(i).contains(inserted.get(i))) return false; return true; }
    public static List<List<String>> getIngredients(String resultId) {
        return RECIPES.stream().filter(r -> r.result.equals(resultId))
                .findFirst().map(Recipe::ingredients).orElse(null);
    }

    private SkewerRecipes() {}
}
