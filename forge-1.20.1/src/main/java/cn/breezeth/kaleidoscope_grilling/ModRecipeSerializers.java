package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, KaleidoscopeGrilling.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SkewerRecipeCraftingRecipe>> SKEWER_RECIPE_BOOK =
            SERIALIZERS.register("skewer_recipe_book",
                    () -> new SimpleCraftingRecipeSerializer<>(SkewerRecipeCraftingRecipe::new));

    private ModRecipeSerializers() {}
}
