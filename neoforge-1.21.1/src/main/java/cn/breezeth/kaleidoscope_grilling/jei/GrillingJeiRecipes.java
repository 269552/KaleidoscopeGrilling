package cn.breezeth.kaleidoscope_grilling.jei;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.SkewerCompatApi;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import cn.breezeth.kaleidoscope_grilling.skewer.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipeBookItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class GrillingJeiRecipes {
  record Threading(List<List<ItemStack>> ingredients, ItemStack result, boolean secret) {}

  record Grilling(List<ItemStack> inputs, List<ItemStack> outputs) {}

  record Seasoning(
      List<ItemStack> required,
      List<ItemStack> optionalChoices,
      int optionalCount,
      ItemStack result) {}

  record OilPress(ItemStack container, ItemStack result) {}

  static List<Threading> fixedThreadingRecipes() {
    List<Threading> result = new ArrayList<>();
    GrillingDataManager.skewersForDisplay().entrySet().stream()
        .filter(entry -> !entry.getValue().ingredients().isEmpty())
        .sorted(Map.Entry.comparingByKey())
        .map(GrillingJeiRecipes::threadingRecipe)
        .filter(java.util.Objects::nonNull)
        .forEach(result::add);

    return List.copyOf(result);
  }

  static List<Threading> secretThreadingRecipes() {
    List<ItemStack> choices = skewerableIngredients();
    if (choices.isEmpty())
      choices =
          itemStacks(
              "minecraft:apple",
              "kaleidoscope_grilling:raw_mantou_slice",
              "kaleidoscope_grilling:beef_chunks");
    if (choices.isEmpty()) return List.of();
    return List.of(
        new Threading(
            List.of(choices, choices, choices), secretThreadingExample(), true));
  }

  static ItemStack secretThreadingExample() {
    return SkeweringHandler.jeiSecretSkewer(
        List.of(
            new ItemStack(Items.APPLE),
            new ItemStack(ModItems.RAW_MANTOU_SLICE.get()),
            new ItemStack(ModItems.BEEF_CHUNKS.get())),
        false);
  }

  static ItemStack fixedThreadingIcon() {
    ResourceLocation id =
        ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeGrilling.MOD_ID, "grilled_beef_skewer");
    return new ItemStack(BuiltInRegistries.ITEM.get(id));
  }

  static List<ItemStack> recordableSkewers() {
    List<ItemStack> skewers =
        fixedThreadingRecipes().stream().map(recipe -> recipe.result().copy()).collect(
            java.util.stream.Collectors.toCollection(ArrayList::new));
    skewers.add(secretThreadingExample());
    return List.copyOf(skewers);
  }

  static List<ItemStack> recordedSkewerBooks() {
    return recordableSkewers().stream()
        .map(
            skewer -> {
              ItemStack book = new ItemStack(ModItems.SKEWER_RECIPE_BOOK.get());
              SkewerRecipeBookItem.setRecipeStack(book, skewer);
              return book;
            })
        .toList();
  }

  static ItemStack blankCookeryRecipe() {
    ResourceLocation id =
        ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "recipe_item");
    return new ItemStack(BuiltInRegistries.ITEM.get(id));
  }

  static List<Grilling> grillingRecipes() {
    List<Grilling> result = new ArrayList<>();
    for (SkewerRecipes.Cooking cooking : SkewerRecipes.cookingRecipes())
      result.add(new Grilling(List.of(cooking.raw()), List.of(cooking.cooked())));
    ItemStack secret = secretThreadingExample();
    if (!secret.isEmpty()) {
      ItemStack cooked = secret.copy();
      SecretSkewerItem.setCooked(cooked, true);
      result.add(new Grilling(List.of(secret), List.of(cooked)));
    }
    return List.copyOf(result);
  }

  static List<ItemStack> oilPots() {
    ResourceLocation id = ResourceLocation.tryParse("kaleidoscope_cookery:oil_pot");
    if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return List.of();
    Item item = BuiltInRegistries.ITEM.get(id);
    List<ItemStack> result = new ArrayList<>();
    for (String type : List.of("", "canola", "secret_chili", "premium_chili")) {
      ItemStack stack = new ItemStack(item);
      OilPotCompat.fill(stack, type, 1);
      OilPotCompat.nameForDisplay(stack);
      result.add(stack);
    }
    return List.copyOf(result);
  }

  static List<ItemStack> filledOilPots() {
    List<ItemStack> pots = oilPots();
    return pots.size() <= 1 ? List.of() : List.copyOf(pots.subList(1, pots.size()));
  }

  static ItemStack premiumChiliOilPot(int points) {
    ResourceLocation id = ResourceLocation.tryParse("kaleidoscope_cookery:oil_pot");
    if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return ItemStack.EMPTY;
    ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
    OilPotCompat.fill(stack, "premium_chili", points);
    OilPotCompat.nameForDisplay(stack);
    return stack;
  }

  static List<Seasoning> seasoningRecipes() {
    List<ItemStack> required =
        itemStacks(
            "kaleidoscope_grilling:green_chili_powder",
            "kaleidoscope_grilling:sichuan_pepper",
            "kaleidoscope_grilling:onion_powder");
    List<ItemStack> choices =
        itemStacks(
            "kaleidoscope_grilling:green_chili_powder",
            "kaleidoscope_grilling:sichuan_pepper",
            "kaleidoscope_grilling:onion_powder",
            "minecraft:redstone",
            "minecraft:gunpowder",
            "kaleidoscope_grilling:houttuynia_powder",
            "kaleidoscope_grilling:totem_powder",
            "kaleidoscope_grilling:dragon_egg_powder");
    ItemStack result = new ItemStack(ModItems.SPECIAL_SEASONING.get());
    SeasoningData.set(
        result,
        List.of(
            "kaleidoscope_grilling:green_chili_powder",
            "kaleidoscope_grilling:sichuan_pepper",
            "kaleidoscope_grilling:onion_powder"));
    return List.of(new Seasoning(required, choices, 5, result));
  }

  static List<OilPress> oilPressRecipes() {
    List<OilPress> recipes = new ArrayList<>();
    ItemStack bucketResult = new ItemStack(ModItems.CANOLA_OIL_BUCKET.get(), 4);
    recipes.add(new OilPress(new ItemStack(Items.BUCKET, 4), bucketResult));

    ResourceLocation oilPotId = ResourceLocation.tryParse("kaleidoscope_cookery:oil_pot");
    if (oilPotId != null && BuiltInRegistries.ITEM.containsKey(oilPotId)) {
      ItemStack emptyPot = new ItemStack(BuiltInRegistries.ITEM.get(oilPotId));
      ItemStack canolaPot = emptyPot.copy();
      OilPotCompat.fill(canolaPot, "canola", 32);
      OilPotCompat.nameForDisplay(canolaPot);
      recipes.add(new OilPress(emptyPot, canolaPot));
    }
    return List.copyOf(recipes);
  }

  private static Threading threadingRecipe(
      Map.Entry<String, GrillingDataManager.Skewer> entry) {
    String output =
        entry.getValue().threadingResult().isBlank()
            ? entry.getKey()
            : entry.getValue().threadingResult();
    ResourceLocation resultId = ResourceLocation.tryParse(output);
    if (resultId == null || !BuiltInRegistries.ITEM.containsKey(resultId)) return null;
    List<List<ItemStack>> ingredients = new ArrayList<>();
    for (List<String> selectors : entry.getValue().ingredients()) {
      List<ItemStack> stacks = selectorStacks(selectors);
      if (stacks.isEmpty()) return null;
      ingredients.add(stacks);
    }
    return new Threading(
        List.copyOf(ingredients), new ItemStack(BuiltInRegistries.ITEM.get(resultId)), false);
  }

  private static List<ItemStack> selectorStacks(List<String> selectors) {
    List<ItemStack> result = new ArrayList<>();
    for (String selector : selectors) {
      if (selector.startsWith("#")) {
        ResourceLocation id = ResourceLocation.tryParse(selector.substring(1));
        if (id == null) continue;
        BuiltInRegistries.ITEM
            .getTag(TagKey.create(Registries.ITEM, id))
            .ifPresent(
                tag -> tag.forEach(holder -> result.add(new ItemStack(holder.value()))));
      } else {
        ResourceLocation id = ResourceLocation.tryParse(selector);
        if (id != null && BuiltInRegistries.ITEM.containsKey(id))
          result.add(new ItemStack(BuiltInRegistries.ITEM.get(id)));
      }
    }
    return result.stream()
        .filter(stack -> !stack.isEmpty())
        .sorted(
            Comparator.comparing(
                stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()))
        .toList();
  }

  private static List<ItemStack> itemStacks(String... ids) {
    List<ItemStack> result = new ArrayList<>();
    for (String value : ids) {
      ResourceLocation id = ResourceLocation.tryParse(value);
      if (id != null && BuiltInRegistries.ITEM.containsKey(id))
        result.add(new ItemStack(BuiltInRegistries.ITEM.get(id)));
    }
    return List.copyOf(result);
  }

  private static List<ItemStack> skewerableIngredients() {
    List<ItemStack> result = new ArrayList<>();
    BuiltInRegistries.ITEM.forEach(
        item -> {
          ItemStack stack = new ItemStack(item);
          try {
            if (!stack.is(SkewerCompatApi.RAW_SKEWERS)
                && !stack.is(SkewerCompatApi.GRILLED_SKEWERS)
                && !stack.is(ModItems.SECRET_SKEWER.get())
                && !stack.is(ModItems.UNFINISHED_SKEWER.get())
                && SkewerCompatApi.canSkewer(stack, null)) result.add(stack);
          } catch (RuntimeException ignored) {
            // A third-party food may require a living entity to evaluate its properties.
          }
        });
    return result;
  }

  private GrillingJeiRecipes() {}
}
