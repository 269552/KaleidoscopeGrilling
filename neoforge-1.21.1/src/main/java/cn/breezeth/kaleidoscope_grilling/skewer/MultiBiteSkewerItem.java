package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;

import cn.breezeth.kaleidoscope_grilling.network.GrillingNetwork;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodHandler;


import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.Nullable;

/** A skewer whose visible bites are completed before its food value is awarded. */
public final class MultiBiteSkewerItem extends SkewerItem {
  /** Holding a skewer for this long commits the meal even when the animation is released early. */
  public static final int MINIMUM_EAT_TICKS = 20;
  private static final Map<LivingEntity, Boolean> COMMITTED_EATS = new WeakHashMap<>();
  public enum AnimationProfile {
    BEEF(90, 0.95833F, 2.33333F, 3.45833F, 4.08333F),
    RAW_ENDER_PEARL(100, 0.95833F, 2.33333F, 3.54167F),
    SQUID_TENTACLE(90, 0.95833F, 2.16667F, 3.5F);

    private final int duration;
    private final float[] biteSeconds;

    AnimationProfile(int duration, float... biteSeconds) {
      this.duration = duration;
      this.biteSeconds = biteSeconds;
    }
  }

  private final AnimationProfile animationProfile;

  public MultiBiteSkewerItem(
      Properties properties,
      @Nullable String tooltipKey,
      @Nullable ResourceLocation effectId,
      int effectDuration,
      AnimationProfile animationProfile) {
    super(properties, tooltipKey, effectId, effectDuration);
    this.animationProfile = animationProfile;
  }

  public AnimationProfile animationProfile() {
    return animationProfile;
  }

  public boolean uses(AnimationProfile profile) {
    return animationProfile == profile;
  }

  public static float visualBiteStage(ItemStack stack, @Nullable LivingEntity entity) {
    if (entity == null
        || !entity.isUsingItem()
        || !ItemStack.isSameItemSameComponents(stack, entity.getUseItem())
        || !(stack.getItem() instanceof MultiBiteSkewerItem animated)) return 0.0F;
    float elapsedSeconds =
        (animated.animationProfile.duration - entity.getUseItemRemainingTicks()) / 20.0F;
    int completedBites = 0;
    for (float biteSecond : animated.animationProfile.biteSeconds) {
      if (elapsedSeconds < biteSecond) break;
      completedBites++;
    }
    return completedBites * 0.25F;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return animationProfile.duration;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);

    InteractionResultHolder<ItemStack> result;
    if (stack.getCount() > 1) {
      ItemStack serving = stack.copyWithCount(1);
      if (!level.isClientSide) {
        ItemStack remainder = stack.copy();
        remainder.shrink(1);
        player.setItemInHand(hand, serving);
        player.getInventory().placeItemBackInInventory(remainder);
      }
      result = super.use(level, player, hand);
    } else {
      result = super.use(level, player, hand);
    }
    if (result.getResult().consumesAction() && player instanceof ServerPlayer serverPlayer)
      GrillingNetwork.setSkewerEatingSound(serverPlayer, animationProfile, true);
    return result;
  }

  @Override
  public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
    stopCustomSound(entity);
    if (!level.isClientSide && consumeCommitted(stack, level, entity)) {
      return;
    }
    super.releaseUsing(stack, level, entity, timeLeft);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    stopCustomSound(entity);
    if (!level.isClientSide && consumeCommitted(stack, level, entity)) return stack;
    return super.finishUsingItem(stack, level, entity);
  }

  /** Completes the food effect exactly at the one-second mark while the visual animation continues. */
  public static void onUseTick(LivingEntityUseItemEvent.Tick event) {
    if (event.getEntity().level().isClientSide
        || !(event.getItem().getItem() instanceof MultiBiteSkewerItem skewer)
        || COMMITTED_EATS.containsKey(event.getEntity())) return;
    int usedTicks = skewer.getUseDuration(event.getItem(), event.getEntity()) - event.getDuration();
    if (usedTicks < MINIMUM_EAT_TICKS) return;

    skewer.finishFoodAndEffect(event.getItem().copyWithCount(1), event.getEntity().level(), event.getEntity());
    HotFoodHandler.finishEarly(event.getItem(), event.getEntity());
    ModAdvancements.recordFoodFinished(event.getEntity(), event.getItem());
    COMMITTED_EATS.put(event.getEntity(), Boolean.TRUE);
  }

  private boolean consumeCommitted(ItemStack stack, Level level, LivingEntity entity) {
    if (COMMITTED_EATS.remove(entity) == null) return false;
    if (entity instanceof Player player && !player.getAbilities().instabuild) stack.shrink(1);
    playBurp(level, entity);
    return true;
  }

  private void stopCustomSound(LivingEntity entity) {
    if (entity instanceof ServerPlayer serverPlayer)
      GrillingNetwork.setSkewerEatingSound(serverPlayer, animationProfile, false);
  }

  @Override
  protected float burpVolume() {
    return 0.8F;
  }
}
