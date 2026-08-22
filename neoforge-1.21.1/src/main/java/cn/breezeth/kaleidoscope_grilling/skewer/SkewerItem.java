package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.food.FoodTooltip;
import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SkewerItem extends Item {
  private final String tooltipKey;
  private final ResourceLocation effectId;
  private final int effectDuration;

  public SkewerItem(
      Properties properties,
      @Nullable String tooltipKey,
      @Nullable ResourceLocation effectId,
      int effectDuration) {
    super(properties);
    this.tooltipKey = tooltipKey;
    this.effectId = effectId;
    this.effectDuration = effectDuration;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 16;
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

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    return finishFoodAndEffect(stack, level, entity);
  }

  /** Completes the skewer through vanilla's eating path, then applies its data-driven effect. */
  protected ItemStack finishFoodAndEffect(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = entity.eat(level, stack);
    if (level.isClientSide) return result;
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    var data = GrillingDataManager.skewerForItem(itemId.toString());
    ResourceLocation resolved =
        data != null && !data.effect().isEmpty() ? ResourceLocation.parse(data.effect()) : effectId;
    int duration = data != null ? data.effectSeconds() * 20 : effectDuration;
    if (resolved != null) {
      BuiltInRegistries.MOB_EFFECT
          .getHolder(ResourceKey.create(Registries.MOB_EFFECT, resolved))
          .ifPresent(effect -> entity.addEffect(new MobEffectInstance(effect, duration)));
    }
    return result;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    FoodTooltip.appendMaxim(tooltip, tooltipKey);
    if (SkewerRecipes.isRawSkewer(stack)) {
      tooltip.add(Component.empty());
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.secret_skewer.raw")
              .withStyle(ChatFormatting.DARK_GRAY));
    }
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    var data = GrillingDataManager.skewerForItem(itemId.toString());
    ResourceLocation resolved =
        data != null && !data.effect().isEmpty() ? ResourceLocation.parse(data.effect()) : effectId;
    int duration = data != null ? data.effectSeconds() * 20 : effectDuration;
    FoodTooltip.appendEffect(tooltip, context, resolved, duration);
  }
}
