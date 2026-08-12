package cn.breezeth.kaleidoscope_grilling;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HotFoodConfig {
  public static final ModConfigSpec SPEC;
  public static final ModConfigSpec.BooleanValue ENABLE_SMELTED_FOOD;
  public static final ModConfigSpec.IntValue SMELTED_FOOD_SECONDS;
  public static final ModConfigSpec.IntValue HOT_SATURATION_PERCENT;
  public static final ModConfigSpec.BooleanValue ENABLE_COOKERY_HEAT_AND_SEASONING;
  public static final ModConfigSpec.BooleanValue ENABLE_SKEWER_GUI_CACHE;
  public static final ModConfigSpec.BooleanValue USE_FIXED_SKEWER_64X_CACHE;
  public static final ModConfigSpec.BooleanValue USE_CUSTOM_SKEWER_64X_CACHE;
  public static final ModConfigSpec.BooleanValue ALLOW_SKEWERS_AT_FULL_HUNGER;
  public static final ModConfigSpec.BooleanValue ENABLE_MAID_GRILLING_TASK;
  public static final ModConfigSpec.IntValue MAID_BUBBLE_COOLDOWN_TICKS;
  public static final ModConfigSpec.DoubleValue MAID_GRILL_ACTION_SPEED_MULTIPLIER;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    builder.push("hot_food").comment("热食系统配置 / Hot Food System");
    ENABLE_COOKERY_HEAT_AND_SEASONING =
        builder
            .comment(
                "【森罗厨房联动热食】允许森罗厨房的炒锅、炖锅为食物附加🔥热状态和调料效果。"
                    + " / Allow Kaleidoscope Cookery pots and stockpots to provide hot-food and"
                    + " special seasoning effects.")
            .define("enableCookeryFoodHeatAndSeasoning", true);
    ENABLE_SMELTED_FOOD =
        builder
            .comment(
                "【熔炉产热食】开启后，从熔炉、烟熏炉取出的可食用成品自动获得🔥热状态。"
                    + " / When enabled, edible furnace and smoker outputs become hot when taken out.")
            .define("enableSmeltedFoodHeat", false);
    SMELTED_FOOD_SECONDS =
        builder
            .comment(
                "【熔炉热食时长】熔炉产出食物的🔥热状态持续时间（秒）。"
                    + " / Hot duration in seconds for edible furnace and smoker outputs.")
            .defineInRange("smeltedFoodSeconds", 30, 1, 86400);
    HOT_SATURATION_PERCENT =
        builder
            .comment(
                "【热食饱和度倍率】食物处于🔥热状态时的饱和度倍率百分比。默认 125%，不改变饱食度。"
                    + " / Saturation multiplier in percent while food is hot. Default 125%."
                    + " Does not change nutrition.")
            .defineInRange("hotSaturationPercent", 125, 100, 200);
    builder.pop();
    builder.push("skewers").comment("Skewer gameplay");
    ALLOW_SKEWERS_AT_FULL_HUNGER =
        builder
            .comment(
                "Allow skewers and plated skewers to be eaten at full hunger."
                    + " Disable this to use vanilla hunger restrictions.")
            .define("allowSkewersAtFullHunger", true);
    builder.pop();
    builder.push("rendering").comment("渲染配置 / Rendering");
    ENABLE_SKEWER_GUI_CACHE =
        builder
            .comment(
                "【烤串GUI缓存】开启后缓存已完成烤串的GUI图标，降低重复生成或模型渲染的性能开销。"
                    + " / Cache completed skewer icons in GUIs to reduce repeated generation or"
                    + " model rendering.")
            .define("enableSkewerGuiCache", true);
    USE_FIXED_SKEWER_64X_CACHE =
        builder
            .comment(
                "【固定串64×64缓存】开启后固定配方串使用生成的64×64 GUI图标；关闭时使用手绘16×16图标（默认）。"
                    + " / Use generated 64x64 GUI icons for fixed-recipe skewers."
                    + " When disabled, fixed skewers use the authored 16x16 icons."
                    + " This does not change custom-skewer caching.")
            .define("useFixedSkewer64xCache", false);
    USE_CUSTOM_SKEWER_64X_CACHE =
        builder
            .comment(
                "【秘制串64×64缓存】开启后秘制烤串使用生成的64×64模型截图；关闭时使用手绘16×16遮罩并按食材取色（默认）。"
                    + " / Use generated 64x64 GUI captures for custom skewers."
                    + " When disabled, custom skewers use the authored 16x16 masks and sampled"
                    + " ingredient colors (default).")
            .define("useCustomSkewer64xCache", false);
    builder.pop();
    builder.push("maid_grilling").comment("Touhou Little Maid grilling task");
    ENABLE_MAID_GRILLING_TASK =
        builder.comment("Enable the optional maid grilling task.").define("enabled", true);
    MAID_BUBBLE_COOLDOWN_TICKS =
        builder
            .comment("Cooldown for repeated maid status bubbles, in ticks.")
            .defineInRange("bubbleCooldownTicks", 600, 20, 72000);
    MAID_GRILL_ACTION_SPEED_MULTIPLIER =
        builder
            .comment("Maid grilling animation and action speed multiplier.")
            .defineInRange("actionSpeedMultiplier", 1.0D, 0.25D, 4.0D);
    builder.pop();
    SPEC = builder.build();
  }

  private HotFoodConfig() {}
}
