package cn.breezeth.kaleidoscope_grilling.kubejs;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import java.lang.reflect.Array;
import java.util.ArrayList;
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

  /** Overrides selected properties of one of Grilling's existing fixed skewers. */
  public static void modifyFixedSkewer(String raw, Map<String, Object> options) {
    FixedSkewerOptions parsed = FixedSkewerOptions.parse(options);
    GrillingDataManager.modifyFixedSkewer(
        parseId(raw, "raw skewer"),
        parsed.ingredients(),
        parsed.effect(),
        parsed.effectSeconds(),
        parsed.rawModel(),
        parsed.cookedModel(),
        parsed.eatingAnimation());
  }

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

  private static List<List<String>> parseFlexibleSlots(Object value) {
    List<Object> rawSlots = elements(value, "ingredients must be an array");
    if (rawSlots.isEmpty() || rawSlots.size() > 3)
      throw new IllegalArgumentException("Threading recipes require 1 to 3 ingredient slots");
    List<List<String>> slots = new ArrayList<>();
    for (Object rawSlot : rawSlots) {
      List<Object> alternatives =
          rawSlot instanceof CharSequence
              ? List.of(rawSlot)
              : elements(rawSlot, "Each ingredient slot must be a string or an array");
      if (alternatives.isEmpty())
        throw new IllegalArgumentException("Threading ingredient slot cannot be empty");
      List<String> selectors = new ArrayList<>();
      for (Object alternative : alternatives) {
        if (!(alternative instanceof CharSequence))
          throw new IllegalArgumentException("Threading ingredient must be a string");
        String selector = String.valueOf(alternative);
        validateSelector(selector);
        selectors.add(selector);
      }
      slots.add(List.copyOf(selectors));
    }
    return List.copyOf(slots);
  }

  private static List<Object> elements(Object value, String error) {
    if (value instanceof Iterable<?> iterable) {
      List<Object> result = new ArrayList<>();
      iterable.forEach(result::add);
      return result;
    }
    if (value != null && value.getClass().isArray()) {
      List<Object> result = new ArrayList<>();
      for (int index = 0; index < Array.getLength(value); index++)
        result.add(Array.get(value, index));
      return result;
    }
    throw new IllegalArgumentException(error);
  }

  private static void validateSelector(String selector) {
    if (selector == null || selector.isBlank())
      throw new IllegalArgumentException("Threading ingredient cannot be blank");
    String id = selector.startsWith("#") ? selector.substring(1) : selector;
    if (ResourceLocation.tryParse(id) == null)
      throw new IllegalArgumentException("Invalid threading ingredient: " + selector);
  }

  private record FixedSkewerOptions(
      List<List<String>> ingredients,
      String effect,
      Integer effectSeconds,
      String rawModel,
      String cookedModel,
      String eatingAnimation) {
    private static FixedSkewerOptions parse(Map<String, Object> values) {
      Map<String, Object> options = values == null ? Map.of() : values;
      String effect = optionalString(options, "effect");
      if (effect != null && !effect.isBlank() && ResourceLocation.tryParse(effect) == null)
        throw new IllegalArgumentException("Invalid effect ID: " + effect);
      return new FixedSkewerOptions(
          options.containsKey("ingredients")
              ? parseFlexibleSlots(options.get("ingredients"))
              : null,
          effect,
          optionalInteger(options, "effectSeconds", "effect_seconds"),
          optionalModel(options, "rawModel", "raw_model"),
          optionalModel(options, "cookedModel", "cooked_model"),
          optionalString(options, "eating", "eatingAnimation"));
    }

    private static String optionalString(Map<String, Object> options, String... keys) {
      for (String key : keys)
        if (options.containsKey(key)) {
          Object value = options.get(key);
          return value == null ? "" : String.valueOf(value);
        }
      return null;
    }

    private static Integer optionalInteger(
        Map<String, Object> options, String key, String alternate) {
      String selected = options.containsKey(key) ? key : options.containsKey(alternate) ? alternate : null;
      if (selected == null) return null;
      Object value = options.get(selected);
      if (value instanceof Number number) return Math.max(0, number.intValue());
      try {
        return Math.max(0, Integer.parseInt(String.valueOf(value)));
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException(selected + " must be an integer", exception);
      }
    }

    private static String optionalModel(
        Map<String, Object> options, String key, String alternate) {
      String value = optionalString(options, key, alternate);
      if (value == null) return null;
      value = value.toLowerCase(java.util.Locale.ROOT);
      if (!value.equals("auto") && !value.equals("generated") && !value.equals("provided"))
        throw new IllegalArgumentException(key + " must be 'auto', 'generated', or 'provided'");
      return value;
    }
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
