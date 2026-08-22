package cn.breezeth.kaleidoscope_grilling.food;

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
  public static final ModConfigSpec.BooleanValue ENABLE_SKEWER_EATING_ANIMATIONS;
  public static final ModConfigSpec.BooleanValue ENABLE_MAID_GRILLING_TASK;
  public static final ModConfigSpec.IntValue MAID_BUBBLE_COOLDOWN_TICKS;
  public static final ModConfigSpec.DoubleValue MAID_GRILL_ACTION_SPEED_MULTIPLIER;

  static {
    ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    builder.push("hot_food").comment("烟火气系统 / Hot-food system");
    ENABLE_COOKERY_HEAT_AND_SEASONING =
        builder
            .comment(
                "【厨房料理烟火气】允许森罗厨房的炒锅、炖锅为成品附加🔥烟火气和调料效果。"
                    + " / Allow Kaleidoscope Cookery pots and stockpots to add the hot-food state"
                    + " and seasoning effects to finished dishes.")
            .define("enableCookeryFoodHeatAndSeasoning", true);
    ENABLE_SMELTED_FOOD =
        builder
            .comment(
                "【熔炉料理烟火气】开启后，从熔炉或烟熏炉取出的可食用成品自动获得🔥烟火气。"
                    + " / When enabled, edible furnace and smoker outputs gain the hot-food state"
                    + " when taken out.")
            .define("enableSmeltedFoodHeat", false);
    SMELTED_FOOD_SECONDS =
        builder
            .comment(
                "【熔炉烟火气时长】熔炉或烟熏炉产出食物的🔥烟火气持续时间（秒）。"
                    + " / Duration in seconds of the hot-food state on edible furnace and smoker"
                    + " outputs.")
            .defineInRange("smeltedFoodSeconds", 30, 1, 86400);
    HOT_SATURATION_PERCENT =
        builder
            .comment(
                "【烟火气饱和度倍率】食物带有🔥烟火气时的饱和度倍率。默认 125%，不改变饱食度。"
                    + " / Saturation multiplier while food has the hot-food state. Defaults to"
                    + " 125% and does not change nutrition.")
            .defineInRange("hotSaturationPercent", 125, 100, 200);
    builder.pop();
    builder.push("skewers").comment("烤串食用 / Skewer eating");
    ALLOW_SKEWERS_AT_FULL_HUNGER =
        builder
            .comment(
                "【满饱食度食用】允许在饱食度已满时继续食用手持烤串和烤串盘中的烤串；关闭后遵循原版限制。"
                    + " / Allow held and plated skewers to be eaten at full hunger. Disable this"
                    + " option to use vanilla hunger restrictions.")
            .define("allowSkewersAtFullHunger", true);
    ENABLE_SKEWER_EATING_ANIMATIONS =
        builder
            .comment(
                "【烤串食用动画】开启时使用烟火的独立食用动画、进度条和专属音效；关闭时改用原版食用方式，"
                    + "完整食用时间仍为 1.25 秒。 / Use Grilling's custom skewer eating animations,"
                    + " progress bar and sounds. When disabled, use vanilla eating while retaining"
                    + " the 1.25-second eating duration.")
            .define("enableEatingAnimations", true);
    builder.pop();
    builder.push("rendering").comment("界面渲染 / GUI rendering");
    ENABLE_SKEWER_GUI_CACHE =
        builder
            .comment(
                "【烤串 GUI 缓存】缓存已完成烤串的 GUI 图标，减少重复生成图标或渲染模型的性能开销。"
                    + " / Cache completed-skewer GUI icons to reduce repeated icon generation and"
                    + " model rendering.")
            .define("enableSkewerGuiCache", true);
    USE_FIXED_SKEWER_64X_CACHE =
        builder
            .comment(
                "【固定串 64×64 缓存】开启后，固定配方串使用生成的 64×64 GUI 图标；关闭后，使用手绘"
                    + " 16×16 图标（默认）。此项不影响秘制串。 / Use generated 64x64 GUI icons for"
                    + " fixed-recipe skewers. When disabled, use the hand-drawn 16x16 icons"
                    + " (default). This option does not affect custom skewers.")
            .define("useFixedSkewer64xCache", false);
    USE_CUSTOM_SKEWER_64X_CACHE =
        builder
            .comment(
                "【秘制串 64×64 缓存】开启后，秘制串使用生成的 64×64 模型截图；关闭后，使用手绘"
                    + " 16×16 遮罩并按食材取色（默认）。 / Use generated 64x64 model captures for"
                    + " custom-skewer GUI icons. When disabled, use the hand-drawn 16x16 masks"
                    + " colored from their ingredients (default).")
            .define("useCustomSkewer64xCache", false);
    builder.pop();
    builder.push("maid_grilling").comment("女仆烧烤 / Maid grilling");
    ENABLE_MAID_GRILLING_TASK =
        builder
            .comment("【启用女仆烧烤】启用可选的女仆烧烤任务。 / Enable the optional maid grilling task.")
            .define("enabled", true);
    MAID_BUBBLE_COOLDOWN_TICKS =
        builder
            .comment(
                "【女仆气泡冷却】同类工作状态气泡再次出现前的冷却时间（游戏刻）。"
                    + " / Cooldown in ticks before the same maid status bubble can appear again.")
            .defineInRange("bubbleCooldownTicks", 600, 20, 72000);
    MAID_GRILL_ACTION_SPEED_MULTIPLIER =
        builder
            .comment(
                "【女仆烧烤速度】女仆烧烤动作和动画的速度倍率。"
                    + " / Speed multiplier for maid grilling actions and animations.")
            .defineInRange("actionSpeedMultiplier", 1.0D, 0.25D, 4.0D);
    builder.pop();
    SPEC = builder.build();
  }

  private HotFoodConfig() {}
}
