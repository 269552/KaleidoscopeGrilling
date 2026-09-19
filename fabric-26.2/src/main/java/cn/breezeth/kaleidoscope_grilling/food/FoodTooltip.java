package cn.breezeth.kaleidoscope_grilling.food;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;

public final class FoodTooltip {
  public static void appendMaxim(List<Component> lines, String key) {
    if (key != null)
      lines.add(
          Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
  }

  public static void appendEffect(
      List<Component> lines, Item.TooltipContext context, ResourceLocation id, int duration) {
    if (id == null || duration <= 0) return;
    BuiltInRegistries.MOB_EFFECT
        .getHolder(ResourceKey.create(Registries.MOB_EFFECT, id))
        .ifPresent(
            effect -> {
              lines.add(CommonComponents.space());
              PotionContents.addPotionTooltip(
                  List.of(new MobEffectInstance(effect, duration)),
                  lines::add,
                  1.0F,
                  context.tickRate());
            });
  }

  public static void appendEffects(
      List<Component> lines,
      Item.TooltipContext context,
      ResourceLocation firstId,
      int firstDuration,
      ResourceLocation secondId,
      int secondDuration) {
    List<MobEffectInstance> effects = new java.util.ArrayList<>();
    addEffect(effects, firstId, firstDuration);
    addEffect(effects, secondId, secondDuration);
    if (effects.isEmpty()) return;
    lines.add(CommonComponents.space());
    PotionContents.addPotionTooltip(effects, lines::add, 1.0F, context.tickRate());
  }

  private static void addEffect(
      List<MobEffectInstance> effects, ResourceLocation id, int duration) {
    if (id == null || duration <= 0) return;
    BuiltInRegistries.MOB_EFFECT
        .getHolder(ResourceKey.create(Registries.MOB_EFFECT, id))
        .ifPresent(effect -> effects.add(new MobEffectInstance(effect, duration)));
  }

  private FoodTooltip() {}
}
