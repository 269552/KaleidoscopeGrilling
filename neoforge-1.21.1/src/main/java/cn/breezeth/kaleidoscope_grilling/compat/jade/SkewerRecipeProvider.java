package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.SkewerRecipeBlockEntity;
import cn.breezeth.kaleidoscope_grilling.SkewerRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

enum SkewerRecipeProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof SkewerRecipeBlockEntity recipe)) return;
        ResourceLocation id = ResourceLocation.tryParse(recipe.recipeResult());
        if (id == null) return;
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
            ItemStack stack = new ItemStack(item);
            tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_recipe.record"));
            tooltip.append(IElementHelper.get().spacer(3, 1));
            tooltip.append(IElementHelper.get().smallItem(stack));
            tooltip.append(IElementHelper.get().spacer(2, 1));
            tooltip.append(item.getDescription());
        });
        List<List<String>> requirements = SkewerRecipes.getIngredients(recipe.recipeResult());
        if (requirements != null && !requirements.isEmpty()) {
            List<ItemStack> ingredients = new ArrayList<>();
            for (List<String> selectors : requirements) {
                BuiltInRegistries.ITEM.stream().map(ItemStack::new)
                        .filter(stack -> selectors.stream().anyMatch(selector -> SkewerRecipes.matchesSelector(stack, selector)))
                        .findFirst().ifPresent(ingredients::add);
            }
            if (!ingredients.isEmpty()) {
                tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_recipe.ingredients"));
                JadeElements.appendItems(tooltip, ingredients);
            }
        }
        tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.wall_usage"));
    }

    @Override public ResourceLocation getUid() { return GrillingJadePlugin.SKEWER_RECIPE; }
}
