package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.food.FoodState;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;


import cn.breezeth.kaleidoscope_grilling.mixin.FoodDataAccessor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class SecretSkewerItem extends Item {
  private static final String COOKED_TAG = "Cooked";
  private static final String CREATOR_TAG = "Creator";
  private static final String CREATOR_NAME_TAG = "CreatorName";
  private static final String CREATOR_UUID_TAG = "CreatorUuid";
  private static final String VISUAL_STAGE_TAG = "ClientVisualStage";

  public SecretSkewerItem(Properties properties) {
    super(properties);
  }

  public static boolean isCooked(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.getUnsafe().getBoolean(COOKED_TAG);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
    if (HotFoodConfig.ALLOW_SKEWERS_AT_FULL_HUNGER.get()
        && !player.getFoodData().needsFood()) {
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(stack);
    }
    return super.use(level, player, hand);
  }

  public static void setCooked(ItemStack stack, boolean cooked) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    if (cooked) tag.putBoolean(COOKED_TAG, true);
    else tag.remove(COOKED_TAG);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static void setVisualStage(ItemStack stack, int stage) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putInt(VISUAL_STAGE_TAG, stage);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static int getVisualStage(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? 0 : data.getUnsafe().getInt(VISUAL_STAGE_TAG);
  }

  public static void setCreator(ItemStack stack, Player player) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putString(CREATOR_NAME_TAG, player.getScoreboardName());
    tag.putUUID(CREATOR_UUID_TAG, player.getUUID());
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static String getCreator(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data != null) {
      CompoundTag tag = data.getUnsafe();
      if (tag.contains(CREATOR_NAME_TAG)) return tag.getString(CREATOR_NAME_TAG);
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
    List<ItemStack> ingredients =
        SkeweringHandler.readEffectiveIngredientStacks(consumed, level.registryAccess());
    ItemStack result = super.finishUsingItem(stack, level, entity);
    if (!level.isClientSide) {
      for (ItemStack ingredient : ingredients) {
        if (ingredient.getItem().getFoodProperties(ingredient, entity) == null) continue;
        FoodSnapshot food = FoodSnapshot.capture(entity);
        ItemStack remainder = ingredient.copyWithCount(1).finishUsingItem(level, entity);
        food.restore(entity);
        if (entity instanceof Player player
            && !remainder.isEmpty()
            && !ItemStack.isSameItemSameComponents(remainder, ingredient))
          player.getInventory().placeItemBackInInventory(remainder);
      }
      level.playSound(
          null,
          entity.blockPosition(),
          SoundEvents.PLAYER_BURP,
          SoundSource.PLAYERS,
          0.5F,
          level.random.nextFloat() * 0.1F + 0.9F);
    }
    return result;
  }

  @Override
  public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
    if (entity == null)
      return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
    List<ItemStack> ingredients =
        SkeweringHandler.readEffectiveIngredientStacks(stack, entity.level().registryAccess());
    List<ItemStack> rawIngredients =
        SkeweringHandler.readIngredientStacks(stack, entity.level().registryAccess());
    if (ingredients.isEmpty()) {
      return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
    }

    int totalNutrition = 0;
    float weightedSaturation = 0;
    int count = 0;

    for (ItemStack ingredient : ingredients) {
      FoodProperties fp = ingredient.getItem().getFoodProperties(ingredient, entity);
      if (fp == null) continue;
      int n = fp.nutrition();
      totalNutrition += n;
      // In 1.21 FoodProperties stores actual saturation points, not the builder modifier.
      weightedSaturation += fp.saturation() * 0.5F;
      count++;
    }

    if (count == 0 || totalNutrition <= 0) {
      return new FoodProperties.Builder().nutrition(1).saturationModifier(0).build();
    }

    float coefficient = 0.6F * (hasDuplicateIngredients(rawIngredients) ? 0.8F : 1.0F);
    int nutrition = Math.max(1, (int) Math.floor(totalNutrition * coefficient));
    float saturation = Math.max(0, weightedSaturation / totalNutrition);
    if (!isCooked(stack)) {
      nutrition = Math.max(1, (int) Math.floor(nutrition * 0.5F));
      saturation *= 0.5F;
    }

    return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    List<ItemStack> ingredients =
        SkeweringHandler.readIngredientStacks(stack, context.registries());
    for (ItemStack ingredient : ingredients) {
      tooltip.add(
          Component.literal("- ").append(ingredient.getHoverName()).withStyle(ChatFormatting.GRAY));
    }
    String creator = getCreator(stack);
    tooltip.add(
        Component.translatable(
                "tooltip.kaleidoscope_grilling.secret_skewer.creator_story",
                creator.isEmpty()
                    ? Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.someone")
                    : Component.literal(creator))
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    Level level = context.level();
    if (level == null || !FoodState.isHot(stack, level)) {
      boolean cooked = isCooked(stack);
      tooltip.add(Component.empty());
      tooltip.add(
          Component.translatable(
                  cooked
                      ? "tooltip.kaleidoscope_grilling.secret_skewer.cooked"
                      : "tooltip.kaleidoscope_grilling.secret_skewer.raw")
              .withStyle(cooked ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
    }
    if (hasDuplicateIngredients(ingredients)) {
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.duplicate_penalty")
              .withStyle(ChatFormatting.DARK_GRAY));
    }
  }

  private static boolean hasDuplicateIngredients(List<ItemStack> ingredients) {
    for (int i = 1; i < ingredients.size(); i++) {
      for (int j = 0; j < i; j++) {
        if (ItemStack.isSameItemSameComponents(ingredients.get(i), ingredients.get(j))) return true;
      }
    }
    return false;
  }

  private record FoodSnapshot(int food, float saturation, float exhaustion) {
    static FoodSnapshot capture(LivingEntity entity) {
      if (!(entity instanceof Player player)) return new FoodSnapshot(0, 0, 0);
      FoodDataAccessor data = (FoodDataAccessor) player.getFoodData();
      return new FoodSnapshot(
          data.grilling$getFoodLevel(),
          data.grilling$getSaturationLevel(),
          data.grilling$getExhaustionLevel());
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
