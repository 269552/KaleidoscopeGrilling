package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class DualEffectFoodItem extends Item {
  private final ResourceLocation firstEffect;
  private final int firstDuration;
  private final ResourceLocation secondEffect;
  private final int secondDuration;
  private final String tooltip;

  public DualEffectFoodItem(
      Properties properties,
      ResourceLocation firstEffect,
      int firstDuration,
      ResourceLocation secondEffect,
      int secondDuration,
      String tooltip) {
    super(properties);
    this.firstEffect = firstEffect;
    this.firstDuration = firstDuration;
    this.secondEffect = secondEffect;
    this.secondDuration = secondDuration;
    this.tooltip = tooltip;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(stack, level, entity);
    if (!level.isClientSide) {
      applyEffect(entity, firstEffect, firstDuration);
      applyEffect(entity, secondEffect, secondDuration);
    }
    return result;
  }

  private static void applyEffect(LivingEntity entity, ResourceLocation id, int duration) {
    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
    if (effect != null) entity.addEffect(new MobEffectInstance(effect, duration));
  }

  @Override
  public void appendHoverText(
      ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
    FoodTooltip.appendMaxim(lines, tooltip);
    FoodTooltip.appendEffects(lines, firstEffect, firstDuration, secondEffect, secondDuration);
  }
}
