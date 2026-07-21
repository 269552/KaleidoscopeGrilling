package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public final class SkewerItem extends Item {
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
    return player.isShiftKeyDown()
        ? InteractionResultHolder.pass(stack)
        : super.use(level, player, hand);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(stack, level, entity);
    if (!level.isClientSide) {
      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
      var data = GrillingDataManager.skewer(itemId.toString());
      ResourceLocation resolved =
          data != null && !data.effect().isEmpty()
              ? ResourceLocation.parse(data.effect())
              : effectId;
      int duration = data != null ? data.effectSeconds() * 20 : effectDuration;
      if (resolved != null) {
        BuiltInRegistries.MOB_EFFECT
            .getHolder(ResourceKey.create(Registries.MOB_EFFECT, resolved))
            .ifPresent(effect -> entity.addEffect(new MobEffectInstance(effect, duration)));
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
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    FoodTooltip.appendMaxim(tooltip, tooltipKey);
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
    var data = GrillingDataManager.skewer(itemId.toString());
    ResourceLocation resolved =
        data != null && !data.effect().isEmpty() ? ResourceLocation.parse(data.effect()) : effectId;
    int duration = data != null ? data.effectSeconds() * 20 : effectDuration;
    FoodTooltip.appendEffect(tooltip, context, resolved, duration);
  }
}
