package cn.breezeth.kaleidoscope_grilling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Public integration API for attaching and querying Kaleidoscope Grilling's hot-food state. */
public final class HotFoodApi {
  public static final int DEFAULT_HEAT_SECONDS = 30;
  private static final Map<Item, Integer> DURATIONS = new ConcurrentHashMap<>();

  /** Registers the default heat duration used by {@link #makeHot(ItemStack, Level)} for an item. */
  public static void registerHeatDuration(Item item, int seconds) {
    if (item == null || seconds <= 0)
      throw new IllegalArgumentException("Hot-food duration must be positive");
    DURATIONS.put(item, seconds);
  }

  public static int getHeatDurationSeconds(ItemStack stack) {
    return DURATIONS.getOrDefault(stack.getItem(), DEFAULT_HEAT_SECONDS);
  }

  public static void makeHot(ItemStack stack, Level level) {
    makeHot(stack, level, getHeatDurationSeconds(stack));
  }

  public static void makeHot(ItemStack stack, Level level, int seconds) {
    if (!stack.isEmpty() && level != null && seconds > 0)
      FoodState.setHot(stack, level.getGameTime() + seconds * 20L);
  }

  public static boolean isHot(ItemStack stack, Level level) {
    return level != null && FoodState.isHot(stack, level);
  }

  public static void season(ItemStack stack, ItemStack seasoning) {
    if (!stack.isEmpty()) SeasoningData.set(stack, SeasoningData.get(seasoning));
  }

  private HotFoodApi() {}
}
