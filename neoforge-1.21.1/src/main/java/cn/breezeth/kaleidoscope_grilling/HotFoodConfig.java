package cn.breezeth.kaleidoscope_grilling;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HotFoodConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLE_SMELTED_FOOD;
  public static final ModConfigSpec.IntValue SMELTED_FOOD_SECONDS;
  public static final ModConfigSpec.IntValue HOT_SATURATION_PERCENT;
  public static final ModConfigSpec.BooleanValue ENABLE_COOKERY_HEAT_AND_SEASONING;
  public static final ModConfigSpec.BooleanValue ENABLE_SKEWER_GUI_CACHE;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    builder.push("hot_food");
    ENABLE_SMELTED_FOOD =
        builder
            .comment("When enabled, edible furnace and smoker outputs become hot when taken out.")
            .define("enableSmeltedFoodHeat", false);
    ENABLE_COOKERY_HEAT_AND_SEASONING =
        builder
            .comment(
                "Allow Kaleidoscope Cookery pots and stockpots to provide hot-food and special"
                    + " seasoning effects.")
            .define("enableCookeryFoodHeatAndSeasoning", true);
    SMELTED_FOOD_SECONDS =
        builder
            .comment("Hot duration in seconds for edible furnace and smoker outputs.")
            .defineInRange("smeltedFoodSeconds", 30, 1, 86400);
    HOT_SATURATION_PERCENT =
        builder
            .comment(
                "Saturation multiplier in percent while food is hot. Default 125%. Does not change"
                    + " nutrition.")
            .defineInRange("hotSaturationPercent", 125, 100, 200);
    builder.pop();
    builder.push("rendering");
    ENABLE_SKEWER_GUI_CACHE =
        builder
            .comment("Cache completed skewer icons in GUIs to reduce repeated model rendering.")
            .define("enableSkewerGuiCache", true);
    builder.pop();
    SPEC = builder.build();
  }

  private HotFoodConfig() {}
}
