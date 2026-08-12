package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
  public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, KaleidoscopeGrilling.MOD_ID);

  public static final DeferredHolder<
          RecipeSerializer<?>, RecipeSerializer<SkewerRecipeCraftingRecipe>>
      SKEWER_RECIPE_BOOK =
          SERIALIZERS.register(
              "skewer_recipe_book",
              () -> new SimpleCraftingRecipeSerializer<>(SkewerRecipeCraftingRecipe::new));
  public static final DeferredHolder<
          RecipeSerializer<?>, RecipeSerializer<ColdHouttuyniaCraftingRecipe>>
      COLD_HOUTTUYNIA =
          SERIALIZERS.register(
              "cold_houttuynia",
              () -> new SimpleCraftingRecipeSerializer<>(ColdHouttuyniaCraftingRecipe::new));
  public static final DeferredHolder<
          RecipeSerializer<?>, RecipeSerializer<ClearSeasoningCraftingRecipe>>
      CLEAR_SEASONING =
          SERIALIZERS.register(
              "clear_seasoning",
              () -> new SimpleCraftingRecipeSerializer<>(ClearSeasoningCraftingRecipe::new));

  private ModRecipeSerializers() {}
}
