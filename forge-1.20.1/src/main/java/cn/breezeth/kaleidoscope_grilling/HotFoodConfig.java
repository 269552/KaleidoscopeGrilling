package cn.breezeth.kaleidoscope_grilling;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HotFoodConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SMELTED_FOOD;
    public static final ForgeConfigSpec.IntValue SMELTED_FOOD_SECONDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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
