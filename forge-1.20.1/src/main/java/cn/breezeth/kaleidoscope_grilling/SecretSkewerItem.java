package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SecretSkewerItem extends Item {
    private static final String COOKED_TAG = "Cooked";
    private static final String CREATOR_TAG = "Creator";

    public SecretSkewerItem(Properties properties) {
        super(properties);
    }

    public static boolean isCooked(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(COOKED_TAG);
    }

    public static void setCooked(ItemStack stack, boolean cooked) {
        stack.getOrCreateTag().putBoolean(COOKED_TAG, cooked);
    }

    public static void setCreator(ItemStack stack, String name) {
        stack.getOrCreateTag().putString(CREATOR_TAG, name);
    }

    public static String getCreator(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(CREATOR_TAG)) {
            return stack.getTag().getString(CREATOR_TAG);
        }
        return "";
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            boolean hot = FoodState.isHot(stack, level);
            List<String> ingredients = SkeweringHandler.readIngredients(stack);
            for (String id : ingredients) {
                Item ingredient = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                if (ingredient == null) continue;
                FoodProperties fp = ingredient.getFoodProperties();
                if (fp == null) continue;
                for (var pair : fp.getEffects()) {
                    MobEffectInstance effect = pair.getFirst();
                    int duration = effect.getDuration();
                    if (hot) duration *= 2;
                    entity.addEffect(new MobEffectInstance(effect.getEffect(), duration,
                            effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
                }
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
            return new FoodProperties.Builder().nutrition(1).saturationMod(0).build();
        }

        int totalNutrition = 0;
        int minNutrition = Integer.MAX_VALUE;
        float totalSaturation = 0;
        int count = 0;
        boolean hasDuplicate = false;

        for (int i = 0; i < ingredients.size(); i++) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(ingredients.get(i)));
            if (item == null) continue;
            FoodProperties fp = item.getFoodProperties();
            if (fp == null) continue;
            int n = fp.getNutrition();
            totalNutrition += n;
            minNutrition = Math.min(minNutrition, n);
            totalSaturation += fp.getSaturationModifier();
            count++;
            // Check for duplicates
            for (int j = 0; j < i; j++) {
                if (ingredients.get(j).equals(ingredients.get(i))) {
                    hasDuplicate = true;
                    break;
                }
            }
        }

        if (count == 0) {
            return new FoodProperties.Builder().nutrition(1).saturationMod(0).build();
        }

        float coefficient = hasDuplicate ? 0.5F : 0.6F;
        int nutrition = Math.max(1, (int) Math.floor((totalNutrition - minNutrition) * coefficient));
        float saturation = Math.min(0.8F, Math.max(0, (totalSaturation / count) * coefficient));

        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!isCooked(stack)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.raw")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        List<String> ingredients = SkeweringHandler.readIngredients(stack);
        for (String id : ingredients) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
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
        if (level != null && FoodState.isHot(stack, level)) {
            tooltip.add(Component.translatable("tooltip.kaleidoscope_grilling.hot")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
