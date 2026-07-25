package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class IngredientTooltipHandler {
  public static void onTooltip(ItemTooltipEvent event) {
    ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
    if (id == null) return;
    String key = key(id.toString());
    if (key != null)
      event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
  }

  private static String key(String id) {
    return switch (id) {
      case "kaleidoscope_cookery:raw_cow_offal" ->
          "tooltip.kaleidoscope_grilling.acquisition.raw_cow_offal";
      case "kaleidoscope_grilling:chicken_skin" ->
          "tooltip.kaleidoscope_grilling.acquisition.chicken_skin";
      case "kaleidoscope_grilling:chicken_wing" ->
          "tooltip.kaleidoscope_grilling.acquisition.chicken_wing";
      case "kaleidoscope_grilling:squid_tentacle" ->
          "tooltip.kaleidoscope_grilling.acquisition.squid_tentacle";
      case "kaleidoscope_grilling:houttuynia" ->
          "tooltip.kaleidoscope_grilling.acquisition.houttuynia";
      case "kaleidoscope_grilling:onion" ->
          "tooltip.kaleidoscope_grilling.acquisition.onion";
      case "kaleidoscope_grilling:sweet_potato" ->
          "tooltip.kaleidoscope_grilling.acquisition.sweet_potato";
      case "kaleidoscope_grilling:canola_seeds" ->
          "tooltip.kaleidoscope_grilling.acquisition.canola_seeds";
      default -> null;
    };
  }

  private IngredientTooltipHandler() {}
}
