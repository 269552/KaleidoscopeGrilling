package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;
import cn.breezeth.kaleidoscope_grilling.food.ColdHouttuyniaCraftingRecipe;
import cn.breezeth.kaleidoscope_grilling.seasoning.ClearSeasoningCraftingRecipe;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipeCraftingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class ModRecipeSerializers {
  public static final RegistryRef<RecipeSerializer<SkewerRecipeCraftingRecipe>> SKEWER_RECIPE_BOOK =
      register("skewer_recipe_book", new SimpleCraftingRecipeSerializer<>(SkewerRecipeCraftingRecipe::new));
  public static final RegistryRef<RecipeSerializer<ColdHouttuyniaCraftingRecipe>> COLD_HOUTTUYNIA =
      register("cold_houttuynia", new SimpleCraftingRecipeSerializer<>(ColdHouttuyniaCraftingRecipe::new));
  public static final RegistryRef<RecipeSerializer<ClearSeasoningCraftingRecipe>> CLEAR_SEASONING =
      register("clear_seasoning", new SimpleCraftingRecipeSerializer<>(ClearSeasoningCraftingRecipe::new));

  private static <T extends RecipeSerializer<?>> RegistryRef<T> register(String name, T serializer) {
    Identifier id = Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name);
    ResourceKey<RecipeSerializer<?>> key = ResourceKey.create(Registries.RECIPE_SERIALIZER, id);
    return RegistryRef.of(Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, key, serializer));
  }

  public static void init() {}

  private ModRecipeSerializers() {}
}
