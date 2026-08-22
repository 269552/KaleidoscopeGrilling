package cn.breezeth.kaleidoscope_grilling.kubejs;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/** KubeJS bindings for data-driven skewer creation and compatibility. */
public final class GrillingKubeJSBindings {
  private static final ResourceLocation GENERATED_COOKED =
      new ResourceLocation("kaleidoscope_grilling", "secret_skewer");

  private GrillingKubeJSBindings() {}

  /** Registers a recipe with a deterministic generated ID. */
  public static void threadingRecipe(String result, String[] ingredients) {
    ResourceLocation resultId = parseId(result, "result");
    List<List<String>> slots = parseSlots(ingredients);
    String hash = Integer.toUnsignedString(Objects.hash(result, Arrays.hashCode(ingredients)), 36);
    ResourceLocation recipeId =
        new ResourceLocation(
            "kaleidoscope_grilling",
            "kubejs/" + resultId.getNamespace() + "/" + resultId.getPath() + "_" + hash);
    GrillingDataManager.registerThreadingRecipe(recipeId, slots, resultId);
  }

  /** Registers a recipe with an explicit ID for stable replacement/management. */
  public static void threadingRecipe(String id, String result, String[] ingredients) {
    ResourceLocation recipeId = parseId(id, "id");
    ResourceLocation resultId = parseId(result, "result");
    GrillingDataManager.registerThreadingRecipe(recipeId, parseSlots(ingredients), resultId);
  }

  /**
   * Registers a recipe whose completed stick-threading result is owned by another mod.
   * Each slot accepts one item ID or tag selector; a leading '#' denotes an item tag.
   */
  public static void threadingRecipe(
      String id, String result, String slot1, String slot2, String slot3) {
    threadingRecipe(id, result, new String[] {slot1, slot2, slot3});
  }

  /** Modes 1 and 2: ingredients become a raw item, which grills into a cooked item. */
  public static void skewerRecipe(String raw, String cooked, String[] ingredients) {
    skewerRecipe(raw, cooked, ingredients, Map.of());
  }

  public static void skewerRecipe(
      String raw, String cooked, String[] ingredients, Map<String, Object> options) {
    ResourceLocation rawId = parseId(raw, "raw result");
    ResourceLocation cookedId = parseId(cooked, "cooked result");
    RecipeOptions parsed = RecipeOptions.parse(options);
    GrillingDataManager.registerSkewerRecipe(
        rawId,
        cookedId,
        parseSlots(ingredients),
        parsed.effect(),
        parsed.effectSeconds(),
        parsed.rawModel(),
        parsed.cookedModel(),
        parsed.eatingAnimation());
  }

  /** Alias used when both item ids were created with the Grilling KubeJS item type. */
  public static void createdSkewerRecipe(String raw, String cooked, String[] ingredients) {
    skewerRecipe(raw, cooked, ingredients);
  }

  public static void createdSkewerRecipe(
      String raw, String cooked, String[] ingredients, Map<String, Object> options) {
    skewerRecipe(raw, cooked, ingredients, options);
  }

  /** Mode 3: an existing raw item grills into an existing cooked item. */
  public static void cookingRecipe(String raw, String cooked) {
    cookingRecipe(raw, cooked, new String[0], Map.of());
  }

  public static void cookingRecipe(
      String raw, String cooked, Map<String, Object> options) {
    cookingRecipe(raw, cooked, new String[0], options);
  }

  public static void cookingRecipe(
      String raw, String cooked, String[] displayIngredients) {
    cookingRecipe(raw, cooked, displayIngredients, Map.of());
  }

  public static void cookingRecipe(
      String raw,
      String cooked,
      String[] displayIngredients,
      Map<String, Object> options) {
    RecipeOptions parsed = RecipeOptions.parse(options);
    GrillingDataManager.registerCookingRecipe(
        parseId(raw, "raw item"),
        parseId(cooked, "cooked item"),
        parseOptionalSlots(displayIngredients),
        parsed.effect(),
        parsed.effectSeconds(),
        parsed.rawModel(),
        parsed.cookedModel(),
        parsed.eatingAnimation());
  }

  /** Mode 3 variant: the existing raw item becomes Grilling's dynamic cooked skewer. */
  public static void generatedCookingRecipe(String raw, String[] displayIngredients) {
    generatedCookingRecipe(raw, displayIngredients, Map.of());
  }

  public static void generatedCookingRecipe(
      String raw, String[] displayIngredients, Map<String, Object> options) {
    RecipeOptions parsed = RecipeOptions.parse(options);
    List<List<String>> slots = parseOptionalSlots(displayIngredients);
    if (slots.isEmpty())
      throw new IllegalArgumentException(
          "generatedCookingRecipe requires 1 to 3 display ingredients");
    GrillingDataManager.registerCookingRecipe(
        parseId(raw, "raw item"),
        GENERATED_COOKED,
        slots,
        parsed.effect(),
        parsed.effectSeconds(),
        parsed.rawModel(),
        "generated",
        parsed.eatingAnimation());
  }

  private static ResourceLocation parseId(String value, String field) {
    ResourceLocation id = ResourceLocation.tryParse(value);
    if (id == null) throw new IllegalArgumentException("Invalid " + field + " item ID: " + value);
    return id;
  }

  private static List<List<String>> parseSlots(String[] ingredients) {
    if (ingredients == null || ingredients.length < 1 || ingredients.length > 3)
      throw new IllegalArgumentException("Threading recipes require 1 to 3 ingredient slots");
    return Arrays.stream(ingredients)
        .map(
            selector -> {
              if (selector == null || selector.isBlank())
                throw new IllegalArgumentException("Threading ingredient cannot be blank");
              String id = selector.startsWith("#") ? selector.substring(1) : selector;
              if (ResourceLocation.tryParse(id) == null)
                throw new IllegalArgumentException("Invalid threading ingredient: " + selector);
              return List.of(selector);
            })
        .toList();
  }

  private static List<List<String>> parseOptionalSlots(String[] ingredients) {
    if (ingredients == null || ingredients.length == 0) return List.of();
    return parseSlots(ingredients);
  }

  private record RecipeOptions(
      String effect,
      int effectSeconds,
      String rawModel,
      String cookedModel,
      String eatingAnimation) {
    private static RecipeOptions parse(Map<String, Object> values) {
      Map<String, Object> options = values == null ? Map.of() : values;
      String effect = string(options, "effect", "");
      if (!effect.isBlank() && ResourceLocation.tryParse(effect) == null)
        throw new IllegalArgumentException("Invalid effect ID: " + effect);
      return new RecipeOptions(
          effect,
          integer(options, "effectSeconds", integer(options, "effect_seconds", 0)),
          model(options, "rawModel", "raw_model"),
          model(options, "cookedModel", "cooked_model"),
          string(options, "eating", string(options, "eatingAnimation", "default")));
    }

    private static String model(Map<String, Object> options, String key, String alternate) {
      String value = string(options, key, string(options, alternate, "auto")).toLowerCase();
      if (!value.equals("auto") && !value.equals("generated") && !value.equals("provided"))
        throw new IllegalArgumentException(
            key + " must be 'auto', 'generated', or 'provided'");
      return value;
    }

    private static String string(Map<String, Object> options, String key, String fallback) {
      Object value = options.get(key);
      return value == null ? fallback : String.valueOf(value);
    }

    private static int integer(Map<String, Object> options, String key, int fallback) {
      Object value = options.get(key);
      if (value == null) return fallback;
      if (value instanceof Number number) return Math.max(0, number.intValue());
      try {
        return Math.max(0, Integer.parseInt(String.valueOf(value)));
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException(key + " must be an integer", exception);
      }
    }
  }
}
