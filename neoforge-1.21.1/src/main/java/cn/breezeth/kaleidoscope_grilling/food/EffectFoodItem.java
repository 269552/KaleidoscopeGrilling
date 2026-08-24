package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class EffectFoodItem extends Item {
  private final ResourceLocation effect;
  private final int duration;
  private final String tooltip;

  public EffectFoodItem(Properties p, ResourceLocation e, int d, String t) {
    super(p);
    effect = e;
    duration = d;
    tooltip = t;
  }

  @Override
  public @Nullable FoodProperties getFoodProperties(
      ItemStack stack, @Nullable LivingEntity entity) {
    return CuisineQualitySupport.foodProperties(stack, super.getFoodProperties(stack, entity));
  }

  @Override
  public ItemStack finishUsingItem(ItemStack s, Level l, LivingEntity entity) {
    int effectDuration = CuisineQualitySupport.effectDuration(s, duration);
    ItemStack result = super.finishUsingItem(s, l, entity);
    if (!l.isClientSide)
      BuiltInRegistries.MOB_EFFECT
          .getHolder(ResourceKey.create(Registries.MOB_EFFECT, effect))
          .ifPresent(h -> entity.addEffect(new MobEffectInstance(h, effectDuration)));
    return result;
  }

  @Override
  public void appendHoverText(ItemStack s, TooltipContext c, List<Component> lines, TooltipFlag f) {
    FoodTooltip.appendMaxim(lines, tooltip);
    CuisineQualitySupport.appendTooltip(s, lines);
    FoodTooltip.appendEffect(
        lines, c, effect, CuisineQualitySupport.effectDuration(s, duration));
  }
}
