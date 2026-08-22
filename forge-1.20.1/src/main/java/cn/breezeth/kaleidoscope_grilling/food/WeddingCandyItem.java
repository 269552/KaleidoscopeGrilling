package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.ChatFormatting;
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

public final class WeddingCandyItem extends Item {
  private static final int EFFECT_DURATION = 15 * 20;
  private final ResourceLocation effect;

  public WeddingCandyItem(Properties properties, ResourceLocation effect) {
    super(properties);
    this.effect = effect;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(stack, level, entity);
    if (!level.isClientSide) {
      MobEffect resolved = ForgeRegistries.MOB_EFFECTS.getValue(effect);
      if (resolved != null) entity.addEffect(new MobEffectInstance(resolved, EFFECT_DURATION));
    }
    return result;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
    for (int line = 1; line <= 2; line++) {
      lines.add(
          Component.translatable("tooltip.kaleidoscope_grilling.wedding_candy." + line)
              .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
    FoodTooltip.appendEffect(lines, effect, EFFECT_DURATION);
  }
}
