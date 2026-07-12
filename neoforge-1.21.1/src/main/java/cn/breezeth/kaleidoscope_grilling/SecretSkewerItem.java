package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import cn.breezeth.kaleidoscope_grilling.mixin.FoodDataAccessor;

public final class SecretSkewerItem extends Item {
    private static final String COOKED_TAG = "Cooked";
    private static final String CREATOR_TAG = "Creator";

    public SecretSkewerItem(Properties properties) {
        super(properties);
    }

    public static boolean isCooked(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(COOKED_TAG);
    }

    public static void setCooked(ItemStack stack, boolean cooked) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(COOKED_TAG, cooked);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setCreator(ItemStack stack, String name) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(CREATOR_TAG, name);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static String getCreator(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains(CREATOR_TAG)) return tag.getString(CREATOR_TAG);
        }
        return "";
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 16;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack consumed = stack.copy();
        List<ItemStack> ingredients = SkeweringHandler.readIngredientStacks(consumed, level.registryAccess());
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            boolean hot = FoodState.isHot(consumed, level);
            for (ItemStack ingredient : ingredients) {
                if (ingredient.getItem().getFoodProperties(ingredient, entity) == null) continue;
                Map<Holder<MobEffect>, Integer> beforeEffects = effectDurations(entity);
                FoodSnapshot food = FoodSnapshot.capture(entity);
                ItemStack remainder = ingredient.copyWithCount(1).finishUsingItem(level, entity);
                food.restore(entity);
                if (hot) doubleNewEffectDurations(entity, beforeEffects);
                if (entity instanceof Player player && !remainder.isEmpty()
                        && !ItemStack.isSameItemSameComponents(remainder, ingredient))
                    player.getInventory().placeItemBackInInventory(remainder);
            }
            FoodState.applySeasoning(consumed, level, entity);
            level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return result;
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        if (entity == null) return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
        List<ItemStack> ingredients = SkeweringHandler.readIngredientStacks(stack, entity.level().registryAccess());
        if (ingredients.isEmpty()) {
            return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
        }

        int totalNutrition = 0;
        int minNutrition = Integer.MAX_VALUE;
        float totalSaturation = 0;
        int count = 0;
        boolean hasDuplicate = false;

        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack ingredient = ingredients.get(i);
            FoodProperties fp = ingredient.getItem().getFoodProperties(ingredient, entity);
            if (fp == null) continue;
            int n = fp.nutrition();
            totalNutrition += n;
            minNutrition = Math.min(minNutrition, n);
            totalSaturation += fp.saturation();
            count++;
            for (int j = 0; j < i; j++) {
                if (ItemStack.isSameItemSameComponents(ingredients.get(j), ingredient)) {
                    hasDuplicate = true;
                    break;
                }
            }
        }

        if (count == 0) {
            return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
        }

        float coefficient = hasDuplicate ? 0.5F : 0.6F;
        int nutrition = Math.max(1, (int) Math.floor((totalNutrition - minNutrition) * coefficient));
        float saturation = Math.min(0.8F, Math.max(0, (totalSaturation / count) * coefficient));
        if (!isCooked(stack)) {
            nutrition = Math.max(1, (int) Math.floor(nutrition * 0.5F));
            saturation *= 0.5F;
        }

        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!isCooked(stack)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.raw")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        List<ItemStack> ingredients = SkeweringHandler.readIngredientStacks(stack, context.registries());
        for (ItemStack ingredient : ingredients) {
            tooltip.add(Component.literal("- ").append(ingredient.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
        String creator = getCreator(stack);
        if (!creator.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.creator", creator)
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        Level level = context != null ? context.level() : null;
        if (level != null && FoodState.isHot(stack, level)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.hot")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    private static Map<Holder<MobEffect>, Integer> effectDurations(LivingEntity entity) {
        Map<Holder<MobEffect>, Integer> result = new HashMap<>();
        for (MobEffectInstance effect : entity.getActiveEffects()) result.put(effect.getEffect(), effect.getDuration());
        return result;
    }

    private static void doubleNewEffectDurations(LivingEntity entity, Map<Holder<MobEffect>, Integer> before) {
        for (MobEffectInstance effect : List.copyOf(entity.getActiveEffects())) {
            int oldDuration = before.getOrDefault(effect.getEffect(), 0);
            if (effect.getDuration() <= oldDuration) continue;
            int duration = oldDuration + (effect.getDuration() - oldDuration) * 2;
            entity.addEffect(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(),
                    effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }
    }

    private record FoodSnapshot(int food, float saturation, float exhaustion) {
        static FoodSnapshot capture(LivingEntity entity) {
            if (!(entity instanceof Player player)) return new FoodSnapshot(0, 0, 0);
            FoodDataAccessor data = (FoodDataAccessor) player.getFoodData();
            return new FoodSnapshot(data.grilling$getFoodLevel(), data.grilling$getSaturationLevel(), data.grilling$getExhaustionLevel());
        }

        void restore(LivingEntity entity) {
            if (!(entity instanceof Player player)) return;
            FoodDataAccessor data = (FoodDataAccessor) player.getFoodData();
            data.grilling$setFoodLevel(food);
            data.grilling$setSaturationLevel(saturation);
            data.grilling$setExhaustionLevel(exhaustion);
        }
    }
}
