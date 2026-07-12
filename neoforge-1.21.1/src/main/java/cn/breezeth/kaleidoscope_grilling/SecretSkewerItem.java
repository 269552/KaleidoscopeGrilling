package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            boolean hot = FoodState.isHot(stack, level);
            List<String> ingredients = SkeweringHandler.readIngredients(stack);
            for (String id : ingredients) {
                BuiltInRegistries.ITEM.getHolder(ResourceKey.create(Registries.ITEM, ResourceLocation.parse(id)))
                        .ifPresent(holder -> {
                            Item ingredient = holder.value();
                            FoodProperties fp = ingredient.getFoodProperties(stack, entity);
                            if (fp == null) return;
                            for (FoodProperties.PossibleEffect possible : fp.effects()) {
                                MobEffectInstance effect = possible.effect();
                                int duration = effect.getDuration();
                                if (hot) duration *= 2;
                                entity.addEffect(new MobEffectInstance(effect.getEffect(), duration,
                                        effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
                            }
                        });
            }
            FoodState.applySeasoning(stack, level, entity);
            level.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return result;
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        List<String> ingredients = SkeweringHandler.readIngredients(stack);
        if (ingredients.isEmpty()) {
            return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
        }

        int totalNutrition = 0;
        int minNutrition = Integer.MAX_VALUE;
        float totalSaturation = 0;
        int count = 0;
        boolean hasDuplicate = false;

        for (int i = 0; i < ingredients.size(); i++) {
            ResourceLocation id = ResourceLocation.parse(ingredients.get(i));
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) continue;
            FoodProperties fp = item.getFoodProperties(stack, entity);
            if (fp == null) continue;
            int n = fp.nutrition();
            totalNutrition += n;
            minNutrition = Math.min(minNutrition, n);
            totalSaturation += fp.saturation();
            count++;
            for (int j = 0; j < i; j++) {
                if (ingredients.get(j).equals(ingredients.get(i))) {
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

        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!isCooked(stack)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.raw")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        List<String> ingredients = SkeweringHandler.readIngredients(stack);
        for (String id : ingredients) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            if (item != null) {
                tooltip.add(Component.literal("- ").append(item.getDescription())
                        .withStyle(ChatFormatting.GRAY));
            }
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
}
