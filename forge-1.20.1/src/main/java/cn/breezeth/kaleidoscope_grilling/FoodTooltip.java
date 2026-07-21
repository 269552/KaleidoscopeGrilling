package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

public final class FoodTooltip {
  public static void appendMaxim(List<Component> lines, String key) {
    if (key != null)
      lines.add(
          Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
  }

  public static void appendEffect(List<Component> lines, ResourceLocation id, int duration) {
    if (id == null || duration <= 0) return;
    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
    if (effect == null) return;
    lines.add(CommonComponents.space());
    PotionUtils.addPotionTooltip(List.of(new MobEffectInstance(effect, duration)), lines, 1.0F);
  }

  public static void appendEffects(
      List<Component> lines,
      ResourceLocation firstId,
      int firstDuration,
      ResourceLocation secondId,
      int secondDuration) {
    java.util.ArrayList<MobEffectInstance> effects = new java.util.ArrayList<>();
    addEffect(effects, firstId, firstDuration);
    addEffect(effects, secondId, secondDuration);
    if (effects.isEmpty()) return;
    lines.add(CommonComponents.space());
    PotionUtils.addPotionTooltip(effects, lines, 1.0F);
  }

  private static void addEffect(
      List<MobEffectInstance> effects, ResourceLocation id, int duration) {
    if (id == null || duration <= 0) return;
    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
    if (effect != null) effects.add(new MobEffectInstance(effect, duration));
  }

  private FoodTooltip() {}
}
