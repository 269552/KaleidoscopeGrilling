package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import cn.breezeth.kaleidoscope_grilling.network.GrillingNetwork;
import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/** Adds optional Grilling eating behavior to existing external cooked items. */
public final class ScriptSkewerEatingHandler {
  public static void onStart(LivingEntityUseItemEvent.Start event) {
    if (event.getItem().getItem() instanceof MultiBiteSkewerItem) return;
    if (!HotFoodConfig.ENABLE_SKEWER_EATING_ANIMATIONS.get()) {
      if (SkewerRecipes.configuredAnimationProfile(event.getItem()) != null)
        event.setDuration(MultiBiteSkewerItem.MINIMUM_EAT_TICKS);
      return;
    }
    MultiBiteSkewerItem.AnimationProfile profile =
        SkewerRecipes.animationProfile(event.getItem());
    if (profile == null) return;
    event.setDuration(profile.duration());
    if (!event.getEntity().level().isClientSide
        && event.getEntity() instanceof ServerPlayer player)
      GrillingNetwork.setSkewerEatingSound(player, profile, true);
  }

  public static void onStop(LivingEntityUseItemEvent.Stop event) {
    if (event.getItem().getItem() instanceof MultiBiteSkewerItem) return;
    MultiBiteSkewerItem.AnimationProfile profile =
        SkewerRecipes.animationProfile(event.getItem());
    if (profile == null) return;
    if (!event.getEntity().level().isClientSide
        && event.getEntity() instanceof ServerPlayer player)
      GrillingNetwork.setSkewerEatingSound(player, profile, false);
    int usedTicks = profile.duration() - event.getDuration();
    if (event.getEntity().level().isClientSide
        || usedTicks < MultiBiteSkewerItem.MINIMUM_EAT_TICKS) return;
    ItemStack original = event.getItem();
    ItemStack consumed = original.copyWithCount(1);
    ItemStack result = original.finishUsingItem(event.getEntity().level(), event.getEntity());
    result = EventHooks.onItemUseFinish(event.getEntity(), consumed, 0, result);
    event.getEntity().setItemInHand(event.getHand(), result);
    event.setCanceled(true);
  }

  public static void onFinish(LivingEntityUseItemEvent.Finish event) {
    if (event.getItem().getItem() instanceof MultiBiteSkewerItem) return;
    MultiBiteSkewerItem.AnimationProfile profile =
        SkewerRecipes.animationProfile(event.getItem());
    if (!event.getEntity().level().isClientSide
        && profile != null
        && event.getEntity() instanceof ServerPlayer player)
      GrillingNetwork.setSkewerEatingSound(player, profile, false);
    if (event.getEntity().level().isClientSide || !SkewerRecipes.isCookedSkewer(event.getItem()))
      return;
    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(event.getItem().getItem());
    GrillingDataManager.Skewer data = GrillingDataManager.skewerForItem(itemId.toString());
    if (data == null || data.effect().isBlank() || data.effectSeconds() <= 0) return;
    ResourceLocation effectId = ResourceLocation.tryParse(data.effect());
    if (effectId == null) return;
    BuiltInRegistries.MOB_EFFECT
        .getHolder(ResourceKey.create(Registries.MOB_EFFECT, effectId))
        .ifPresent(
            effect ->
                event
                    .getEntity()
                    .addEffect(new MobEffectInstance(effect, data.effectSeconds() * 20)));
  }

  private ScriptSkewerEatingHandler() {}
}
