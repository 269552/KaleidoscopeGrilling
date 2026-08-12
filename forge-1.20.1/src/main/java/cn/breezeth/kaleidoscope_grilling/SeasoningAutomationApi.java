package cn.breezeth.kaleidoscope_grilling;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Player-independent planning API for filling and shaking seasoning bottles. */
public final class SeasoningAutomationApi {
  public static final int CAPACITY = 8;
  private static final List<String> BASE_INGREDIENTS =
      List.of(
          "kaleidoscope_grilling:green_chili_powder",
          "kaleidoscope_grilling:sichuan_pepper",
          "kaleidoscope_grilling:onion_powder");

  public record SlotTake(int slot, int amount) {}

  public record MixPlan(List<SlotTake> takes, List<String> ingredients, boolean pendingBottle) {
    public MixPlan {
      takes = List.copyOf(takes);
      ingredients = List.copyOf(ingredients);
    }
  }

  public static MixPlan plan(List<ItemStack> stacks) {
    for (int slot = 0; slot < stacks.size(); slot++) {
      ItemStack stack = stacks.get(slot);
      if (stack.is(ModItems.PENDING_SEASONING.get())) {
        List<String> stored = SeasoningData.get(stack);
        if (hasBase(stored) && stored.size() <= CAPACITY)
          return new MixPlan(List.of(new SlotTake(slot, 1)), stored, true);
      }
    }

    int bottleSlot = -1;
    for (int slot = 0; slot < stacks.size(); slot++) {
      if (stacks.get(slot).is(ModItems.EMPTY_SEASONING_BOTTLE.get())) {
        bottleSlot = slot;
        break;
      }
    }
    if (bottleSlot < 0) return null;

    int[] reserved = new int[stacks.size()];
    List<SlotTake> takes = new ArrayList<>();
    List<String> ingredients = new ArrayList<>();
    takes.add(new SlotTake(bottleSlot, 1));
    reserved[bottleSlot]++;

    for (String required : BASE_INGREDIENTS) {
      int slot = findAvailable(stacks, reserved, required);
      if (slot < 0) return null;
      reserved[slot]++;
      takes.add(new SlotTake(slot, 1));
      ingredients.add(required);
    }

    appendOptional(stacks, reserved, takes, ingredients, false);
    appendOptional(stacks, reserved, takes, ingredients, true);
    return new MixPlan(takes, ingredients, false);
  }

  /** Plans the first satisfiable seasoning recipe in filter-slot order. */
  public static MixPlan plan(List<ItemStack> stacks, List<List<String>> targets) {
    for (List<String> target : targets) {
      if (!isValidTarget(target)) continue;
      MixPlan plan = planTarget(stacks, target);
      if (plan != null) return plan;
    }
    return null;
  }

  public static ItemStack finish(MixPlan plan, RandomSource random) {
    if (plan == null || !hasBase(plan.ingredients()) || plan.ingredients().size() > CAPACITY)
      return ItemStack.EMPTY;
    ItemStack result = new ItemStack(ModItems.SPECIAL_SEASONING.get());
    SeasoningData.set(result, plan.ingredients());
    SeasoningData.setVariant(result, random.nextInt(8));
    SeasoningData.setUses(result, 0);
    return result;
  }

  /** Adds one valid ingredient to an empty or pending bottle without finishing it. */
  public static ItemStack appendIngredient(ItemStack bottle, ItemStack ingredient) {
    if ((!bottle.is(ModItems.EMPTY_SEASONING_BOTTLE.get())
            && !bottle.is(ModItems.PENDING_SEASONING.get()))
        || !isValidIngredient(ingredient)) return ItemStack.EMPTY;
    List<String> ingredients = SeasoningData.get(bottle);
    if (ingredients.size() >= CAPACITY) return ItemStack.EMPTY;

    List<String> updated = new ArrayList<>(ingredients);
    updated.add(itemId(ingredient));
    ItemStack result;
    if (bottle.is(ModItems.PENDING_SEASONING.get()) || hasBase(updated)) {
      result = new ItemStack(ModItems.PENDING_SEASONING.get());
    } else {
      result = bottle.copyWithCount(1);
    }
    SeasoningData.set(result, updated);
    return result;
  }

  public static boolean isValidIngredient(ItemStack stack) {
    if (stack.isEmpty()) return false;
    String id = itemId(stack);
    return BASE_INGREDIENTS.contains(id) || GrillingDataManager.isSeasoningIngredient(id);
  }

  public static boolean hasBase(List<String> ingredients) {
    return ingredients.containsAll(BASE_INGREDIENTS);
  }

  public static boolean isValidTarget(List<String> ingredients) {
    if (ingredients.isEmpty() || ingredients.size() > CAPACITY || !hasBase(ingredients))
      return false;
    for (String id : ingredients) {
      if (!BASE_INGREDIENTS.contains(id) && !GrillingDataManager.isSeasoningIngredient(id))
        return false;
    }
    return true;
  }

  private static MixPlan planTarget(List<ItemStack> stacks, List<String> target) {
    for (int slot = 0; slot < stacks.size(); slot++) {
      ItemStack stack = stacks.get(slot);
      if (stack.is(ModItems.PENDING_SEASONING.get())
          && sameIngredients(SeasoningData.get(stack), target))
        return new MixPlan(List.of(new SlotTake(slot, 1)), target, true);
    }

    int bottleSlot = -1;
    for (int slot = 0; slot < stacks.size(); slot++) {
      if (stacks.get(slot).is(ModItems.EMPTY_SEASONING_BOTTLE.get())) {
        bottleSlot = slot;
        break;
      }
    }
    if (bottleSlot < 0) return null;

    int[] reserved = new int[stacks.size()];
    List<SlotTake> takes = new ArrayList<>();
    takes.add(new SlotTake(bottleSlot, 1));
    reserved[bottleSlot]++;
    for (String id : target) {
      int slot = findAvailable(stacks, reserved, id);
      if (slot < 0) return null;
      reserved[slot]++;
      takes.add(new SlotTake(slot, 1));
    }
    return new MixPlan(takes, target, false);
  }

  private static boolean sameIngredients(List<String> first, List<String> second) {
    if (first.size() != second.size()) return false;
    return ingredientCounts(first).equals(ingredientCounts(second));
  }

  private static Map<String, Integer> ingredientCounts(List<String> ingredients) {
    Map<String, Integer> counts = new HashMap<>();
    for (String id : ingredients) counts.merge(id, 1, Integer::sum);
    return counts;
  }

  private static int findAvailable(List<ItemStack> stacks, int[] reserved, String id) {
    for (int slot = 0; slot < stacks.size(); slot++) {
      ItemStack stack = stacks.get(slot);
      if (id.equals(itemId(stack)) && stack.getCount() > reserved[slot]) return slot;
    }
    return -1;
  }

  private static void appendOptional(
      List<ItemStack> stacks,
      int[] reserved,
      List<SlotTake> takes,
      List<String> ingredients,
      boolean baseIngredients) {
    for (int slot = 0; slot < stacks.size() && ingredients.size() < CAPACITY; slot++) {
      ItemStack stack = stacks.get(slot);
      if (!isValidIngredient(stack)) continue;
      String id = itemId(stack);
      if (BASE_INGREDIENTS.contains(id) != baseIngredients) continue;
      int available = stack.getCount() - reserved[slot];
      if (available <= 0) continue;
      int amount = Math.min(available, CAPACITY - ingredients.size());
      reserved[slot] += amount;
      takes.add(new SlotTake(slot, amount));
      for (int i = 0; i < amount; i++) ingredients.add(id);
    }
  }

  private static String itemId(ItemStack stack) {
    ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
    return id.toString();
  }

  private SeasoningAutomationApi() {}
}
