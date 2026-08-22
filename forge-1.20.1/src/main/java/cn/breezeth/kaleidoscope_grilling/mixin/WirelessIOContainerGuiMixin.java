package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.GrillingWirelessIOData;
import cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid.GrillingWirelessIOModePayload;
import cn.breezeth.kaleidoscope_grilling.network.GrillingNetwork;
import com.github.tartaricacid.touhoulittlemaid.client.gui.item.WirelessIOContainerGui;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.other.WirelessIOContainer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WirelessIOContainerGui.class, remap = false)
abstract class WirelessIOContainerGuiMixin extends AbstractContainerScreen<WirelessIOContainer> {
  protected WirelessIOContainerGuiMixin(
      WirelessIOContainer menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
  }

  // The maid jar is mapped in userdev but keeps the SRG name in production.
  @Inject(method = {"init", "m_7856_"}, at = @At("TAIL"), require = 1)
  private void kaleidoscopeGrilling$addGrillingModeButton(CallbackInfo ci) {
    WirelessIOContainerGui screen = (WirelessIOContainerGui) (Object) this;
    WirelessIOContainer menu = screen.getMenu();
    boolean enabled = GrillingWirelessIOData.isEnabled(menu.getWirelessIO());
    Button[] holder = new Button[1];
    holder[0] =
        Button.builder(
                label(enabled),
                button -> {
                  boolean next = !GrillingWirelessIOData.isEnabled(menu.getWirelessIO());
                  GrillingWirelessIOData.setEnabled(menu.getWirelessIO(), next);
                  button.setMessage(label(next));
                  GrillingNetwork.setGrillingWirelessIOMode(next);
                })
            .bounds(leftPos + 136, topPos + 62, 16, 16)
            .tooltip(
                net.minecraft.client.gui.components.Tooltip.create(
                    Component.translatable(
                        "gui.kaleidoscope_grilling.wireless_io.grilling_mode.tooltip")))
            .build();
    addRenderableWidget(holder[0]);
  }

  private static Component label(boolean enabled) {
    return Component.literal("烤").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY);
  }
}
