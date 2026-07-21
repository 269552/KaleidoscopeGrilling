package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.ModItems;
import cn.breezeth.kaleidoscope_grilling.SkewerRecipeBlockEntity;
import cn.breezeth.kaleidoscope_grilling.SkewerRecipeBookItem;
import cn.breezeth.kaleidoscope_grilling.SkewerRecipes;
import cn.breezeth.kaleidoscope_grilling.SkeweringHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

enum SkewerRecipeProvider implements IBlockComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    if (!(accessor.getBlockEntity() instanceof SkewerRecipeBlockEntity recipe)) return;
    ItemStack recorded = SkewerRecipeBookItem.readRecipeStack(recipe.recipeBook());
    if (recorded.isEmpty()) return;
    {
      tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_recipe.record"));
      tooltip.append(IElementHelper.get().spacer(3, 1));
      tooltip.append(IElementHelper.get().smallItem(recorded));
      tooltip.append(IElementHelper.get().spacer(2, 1));
      tooltip.append(recorded.getHoverName());
    }
    List<ItemStack> custom =
        recorded.is(ModItems.SECRET_SKEWER.get())
            ? SkeweringHandler.readIngredientStacks(recorded, accessor.getLevel().registryAccess())
            : List.of();
    List<List<String>> requirements = SkewerRecipes.getIngredients(recipe.recipeResult());
    if (!custom.isEmpty()) {
      tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_recipe.ingredients"));
      JadeElements.appendItems(tooltip, custom);
    } else if (requirements != null && !requirements.isEmpty()) {
      List<ItemStack> ingredients = new ArrayList<>();
      for (List<String> selectors : requirements) {
        BuiltInRegistries.ITEM.stream()
            .map(ItemStack::new)
            .filter(
                stack ->
                    selectors.stream()
                        .anyMatch(selector -> SkewerRecipes.matchesSelector(stack, selector)))
            .findFirst()
            .ifPresent(ingredients::add);
      }
      if (!ingredients.isEmpty()) {
        tooltip.add(Component.translatable("jade.kaleidoscope_grilling.skewer_recipe.ingredients"));
        JadeElements.appendItems(tooltip, ingredients);
      }
    }
    tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.wall_usage"));
  }

  @Override
  public ResourceLocation getUid() {
    return GrillingJadePlugin.SKEWER_RECIPE;
  }
}
