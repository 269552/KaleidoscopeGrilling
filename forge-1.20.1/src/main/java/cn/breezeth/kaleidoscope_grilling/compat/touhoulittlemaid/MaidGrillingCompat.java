package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public final class MaidGrillingCompat {
  public static void init() {
    MinecraftForge.EVENT_BUS.addListener(MaidGrillingCompat::onWirelessIOTooltip);
  }

  private static void onWirelessIOTooltip(ItemTooltipEvent event) {
    if (!(event.getItemStack().getItem() instanceof ItemWirelessIO)
        || !GrillingWirelessIOData.isEnabled(event.getItemStack())) return;
    boolean output = ItemWirelessIO.isMaidToChest(event.getItemStack());
    if (!event.getToolTip().isEmpty()) {
      event
          .getToolTip()
          .set(
              0,
              Component.translatable(
                      output
                          ? "item.kaleidoscope_grilling.grilling_wireless_io.output"
                          : "item.kaleidoscope_grilling.grilling_wireless_io.supply")
                  .withStyle(ChatFormatting.GOLD));
    }
    event
        .getToolTip()
        .add(
            Component.translatable(
                    output
                        ? "tooltip.kaleidoscope_grilling.grilling_wireless_io.output"
                        : "tooltip.kaleidoscope_grilling.grilling_wireless_io.supply")
                .withStyle(ChatFormatting.GRAY));
    event
        .getToolTip()
        .add(
            Component.translatable("tooltip.kaleidoscope_grilling.grilling_wireless_io.common")
                .withStyle(ChatFormatting.DARK_GRAY));
  }

  private MaidGrillingCompat() {}
}
