package cn.breezeth.kaleidoscope_grilling;

import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackBlockEntity;


import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Public compatibility hooks for advanced-rack seasoning and tool compartments. */
public final class AdvancedRackCompatApi {
  public static final TagKey<Item> SEASONING_ITEMS =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(
              KaleidoscopeGrilling.MOD_ID, "advanced_rack_seasonings"));
  public static final TagKey<Item> TOOL_ITEMS =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(
              KaleidoscopeGrilling.MOD_ID, "advanced_rack_tools"));

  private static final Set<Item> SEASONINGS = new LinkedHashSet<>();
  private static final Set<Item> TOOLS = new LinkedHashSet<>();
  private static final Map<ResourceLocation, Predicate<ItemStack>> SEASONING_RULES =
      new LinkedHashMap<>();
  private static final Map<ResourceLocation, Predicate<ItemStack>> TOOL_RULES =
      new LinkedHashMap<>();

  public static synchronized void registerSeasoningItem(Item item) {
    SEASONINGS.add(Objects.requireNonNull(item, "item"));
  }

  public static synchronized void registerToolItem(Item item) {
    TOOLS.add(Objects.requireNonNull(item, "item"));
  }

  public static synchronized void registerSeasoningRule(
      ResourceLocation id, Predicate<ItemStack> rule) {
    putOnce(SEASONING_RULES, id, rule);
  }

  public static synchronized void registerToolRule(ResourceLocation id, Predicate<ItemStack> rule) {
    putOnce(TOOL_RULES, id, rule);
  }

  public static boolean isSeasoningItem(ItemStack stack) {
    if (stack.isEmpty()) return false;
    if (stack.is(SEASONING_ITEMS)) return true;
    synchronized (AdvancedRackCompatApi.class) {
      if (SEASONINGS.contains(stack.getItem())) return true;
    }
    return seasoningRules().stream().anyMatch(rule -> rule.test(stack));
  }

  public static boolean isToolItem(ItemStack stack) {
    if (stack.isEmpty()) return false;
    if (stack.is(TOOL_ITEMS)) return true;
    synchronized (AdvancedRackCompatApi.class) {
      if (TOOLS.contains(stack.getItem())) return true;
    }
    return toolRules().stream().anyMatch(rule -> rule.test(stack));
  }

  public static boolean canPlace(int slot, ItemStack stack) {
    return slot >= 0
        && slot < AdvancedRackBlockEntity.COMPARTMENT_COUNT
        && (slot < 5 ? isSeasoningItem(stack) : isToolItem(stack));
  }

  private static synchronized List<Predicate<ItemStack>> seasoningRules() {
    return List.copyOf(SEASONING_RULES.values());
  }

  private static synchronized List<Predicate<ItemStack>> toolRules() {
    return List.copyOf(TOOL_RULES.values());
  }

  private static <T> void putOnce(Map<ResourceLocation, T> map, ResourceLocation id, T value) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(value, "rule");
    if (map.putIfAbsent(id, value) != null)
      throw new IllegalArgumentException("Duplicate advanced-rack compatibility handler: " + id);
  }

  private AdvancedRackCompatApi() {}
}
