package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class WeddingCandyItem extends Item {
  private static final int EFFECT_DURATION = 60 * 20;
  private final ResourceLocation effect;

  public WeddingCandyItem(Properties properties, ResourceLocation effect) {
    super(properties);
    this.effect = effect;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(stack, level, entity);
    if (!level.isClientSide) {
      BuiltInRegistries.MOB_EFFECT
          .getHolder(ResourceKey.create(Registries.MOB_EFFECT, effect))
          .ifPresent(holder -> entity.addEffect(new MobEffectInstance(holder, EFFECT_DURATION)));
    }
    return result;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
    for (int line = 1; line <= 3; line++) {
      lines.add(
          Component.translatable("tooltip.kaleidoscope_grilling.wedding_candy." + line)
              .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
    FoodTooltip.appendEffect(lines, context, effect, EFFECT_DURATION);
  }
}
