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
  public ItemStack finishUsingItem(ItemStack s, Level l, LivingEntity entity) {
    ItemStack result = super.finishUsingItem(s, l, entity);
    if (!l.isClientSide) {
      MobEffect e = ForgeRegistries.MOB_EFFECTS.getValue(effect);
      if (e != null) entity.addEffect(new MobEffectInstance(e, duration));
    }
    return result;
  }

  @Override
  public void appendHoverText(ItemStack s, Level l, List<Component> lines, TooltipFlag f) {
    FoodTooltip.appendMaxim(lines, tooltip);
    FoodTooltip.appendEffect(lines, effect, duration);
  }
}
