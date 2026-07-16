package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Public compatibility hooks for skewer ingredients and non-standard cooking conversions. */
public final class SkewerCompatApi {
    public enum Decision { PASS, ALLOW, DENY }
    @FunctionalInterface public interface IngredientRule {
        Decision test(ItemStack stack, @Nullable LivingEntity eater);
    }
    @FunctionalInterface public interface CookingRule {
        ItemStack cook(ItemStack raw);
    }

    public static final TagKey<Item> SKEWERABLE_INGREDIENTS = TagKey.create(Registries.ITEM,
            new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "skewerable_ingredients"));
    public static final TagKey<Item> UNSKEWERABLE_INGREDIENTS = TagKey.create(Registries.ITEM,
            new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "unskewerable_ingredients"));
    public static final TagKey<Item> RAW_SKEWERS = TagKey.create(Registries.ITEM,
            new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "raw_skewers"));

    private static final Map<ResourceLocation, IngredientRule> INGREDIENT_RULES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, CookingRule> COOKING_RULES = new LinkedHashMap<>();

    public static synchronized void registerIngredientRule(ResourceLocation id, IngredientRule rule) {
        putOnce(INGREDIENT_RULES, id, rule);
    }

    public static synchronized void registerCookingRule(ResourceLocation id, CookingRule rule) {
        putOnce(COOKING_RULES, id, rule);
    }

    public static boolean canSkewer(ItemStack stack, @Nullable LivingEntity eater) {
        if (stack.isEmpty() || stack.is(UNSKEWERABLE_INGREDIENTS)) return false;
        for (IngredientRule rule : ingredientRules()) {
            Decision result = rule.test(stack, eater);
            if (result != Decision.PASS) return result == Decision.ALLOW;
        }
        return stack.is(SKEWERABLE_INGREDIENTS) || stack.getItem().getFoodProperties(stack, eater) != null;
    }

    public static ItemStack customCookedResult(ItemStack raw) {
        for (CookingRule rule : cookingRules()) {
            ItemStack result = rule.cook(raw);
            if (result != null && !result.isEmpty()) return result;
        }
        return ItemStack.EMPTY;
    }

    private static synchronized List<IngredientRule> ingredientRules() { return List.copyOf(INGREDIENT_RULES.values()); }
    private static synchronized List<CookingRule> cookingRules() { return List.copyOf(COOKING_RULES.values()); }
    private static <T> void putOnce(Map<ResourceLocation, T> map, ResourceLocation id, T value) {
        Objects.requireNonNull(id, "id"); Objects.requireNonNull(value, "value");
        if (map.putIfAbsent(id, value) != null) throw new IllegalArgumentException("Duplicate skewer compatibility handler: " + id);
    }
    private SkewerCompatApi() {}
}
