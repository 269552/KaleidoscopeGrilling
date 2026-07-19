package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, KaleidoscopeGrilling.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SkewerRecipeCraftingRecipe>> SKEWER_RECIPE_BOOK =
            SERIALIZERS.register("skewer_recipe_book",
                    () -> new SimpleCraftingRecipeSerializer<>(SkewerRecipeCraftingRecipe::new));

    private ModRecipeSerializers() {}
}
