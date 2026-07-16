package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public final class SkewerRecipes {
    private record Recipe(ResourceLocation rawResult, ResourceLocation cookedResult, List<List<String>> ingredients) {}

    private static List<Recipe> recipes() {
        return GrillingDataManager.skewers().entrySet().stream()
                .filter(entry -> !entry.getValue().ingredients().isEmpty())
                .map(SkewerRecipes::recipe)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static Recipe recipe(Map.Entry<String, GrillingDataManager.Skewer> entry) {
        ResourceLocation raw = ResourceLocation.tryParse(entry.getKey());
        if (raw == null) return null;
        String configured = entry.getValue().cookedResult();
        ResourceLocation cooked = configured.isEmpty() ? inferCooked(raw) : ResourceLocation.tryParse(configured);
        return cooked == null ? null : new Recipe(raw, cooked, entry.getValue().ingredients());
    }

    private static ResourceLocation inferCooked(ResourceLocation raw) {
        String path = raw.getPath();
        if (!path.startsWith("raw_")) return null;
        return ResourceLocation.tryParse(raw.getNamespace() + ":grilled_" + path.substring(4));
    }

    public static boolean canAppend(List<ItemStack> inserted, ItemStack next) {
        int slot = inserted.size();
        return recipes().stream().anyMatch(recipe -> slot < recipe.ingredients().size()
                && prefix(recipe, inserted) && matchesAny(next, recipe.ingredients().get(slot)));
    }

    public static boolean isConfiguredIngredient(ItemStack stack) {
        return recipes().stream().flatMap(recipe -> recipe.ingredients().stream())
                .anyMatch(selectors -> matchesAny(stack, selectors));
    }

    public static ResourceLocation completedResult(List<ItemStack> inserted) {
        return recipes().stream()
                .filter(recipe -> recipe.ingredients().size() == inserted.size() && prefix(recipe, inserted))
                .map(Recipe::rawResult)
                .findFirst().orElse(null);
    }

    public static int expectedSize(List<ItemStack> inserted) {
        return recipes().stream().filter(recipe -> prefix(recipe, inserted))
                .mapToInt(recipe -> recipe.ingredients().size()).max().orElse(3);
    }

    public static boolean isRawSkewer(ItemStack stack) {
        if (stack.is(SkewerCompatApi.RAW_SKEWERS)) return true;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return recipes().stream().anyMatch(recipe -> recipe.rawResult().equals(id));
    }

    public static ItemStack cookedResult(ItemStack rawStack) {
        ItemStack custom = SkewerCompatApi.customCookedResult(rawStack);
        if (!custom.isEmpty()) return custom;
        ResourceLocation rawId = BuiltInRegistries.ITEM.getKey(rawStack.getItem());
        ResourceLocation cookedId = recipes().stream()
                .filter(recipe -> recipe.rawResult().equals(rawId))
                .map(Recipe::cookedResult).findFirst().orElse(null);
        if (cookedId == null || !BuiltInRegistries.ITEM.containsKey(cookedId)) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.get(cookedId));
    }

    public static List<List<String>> getIngredients(String id) {
        ResourceLocation full = ResourceLocation.tryParse(id.contains(":") ? id : KaleidoscopeGrilling.MOD_ID + ":" + id);
        if (full == null) return null;
        return recipes().stream().filter(recipe -> recipe.rawResult().equals(full))
                .findFirst().map(Recipe::ingredients).orElse(null);
    }

    public static boolean matchesSelector(ItemStack stack, String selector) {
        if (selector.startsWith("#")) {
            ResourceLocation tagId = ResourceLocation.tryParse(selector.substring(1));
            return tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId));
        }
        ResourceLocation itemId = ResourceLocation.tryParse(selector);
        return itemId != null && itemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean matchesAny(ItemStack stack, List<String> selectors) {
        return selectors.stream().anyMatch(selector -> matchesSelector(stack, selector));
    }

    private static boolean prefix(Recipe recipe, List<ItemStack> inserted) {
        if (inserted.size() > recipe.ingredients().size()) return false;
        for (int i = 0; i < inserted.size(); i++)
            if (!matchesAny(inserted.get(i), recipe.ingredients().get(i))) return false;
        return true;
    }

    private SkewerRecipes() {}
}
