package cn.breezeth.kaleidoscope_grilling;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HotFoodConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_SMELTED_FOOD;
    public static final ModConfigSpec.IntValue SMELTED_FOOD_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hot_food");
        ENABLE_SMELTED_FOOD = builder.comment("When enabled, edible furnace and smoker outputs become hot when taken out.")
                .define("enableSmeltedFood", false);
        SMELTED_FOOD_SECONDS = builder.comment("Hot duration in seconds for edible furnace and smoker outputs.")
                .defineInRange("smeltedFoodSeconds", 30, 1, 86400);
        builder.pop();
        SPEC = builder.build();
    }

    private HotFoodConfig() {}
}
