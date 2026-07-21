package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AdvancedRackScreen extends AbstractContainerScreen<AdvancedRackMenu> {
  private static final int COMPARTMENT_BACKGROUND = 0x80666666;
  private static final ResourceLocation TEXTURE =
      new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "textures/gui/advanced_rack_settings.png");
  private static final ResourceLocation DEPOSIT_TEXTURE =
      new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "textures/gui/advanced_rack_deposit.png");
  private static final ResourceLocation DEPOSIT_SELECTED_TEXTURE =
      new ResourceLocation(
          KaleidoscopeGrilling.MOD_ID, "textures/gui/advanced_rack_deposit_selected.png");
  private boolean depositHovered;

  public AdvancedRackScreen(AdvancedRackMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 194;
    imageHeight = 235;
  }

  @Override
  protected void init() {
    super.init();
    leftPos = width / 2 - 106;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    depositHovered = isDepositAt(mouseX, mouseY);
    graphics.blit(
        depositHovered ? DEPOSIT_SELECTED_TEXTURE : DEPOSIT_TEXTURE,
        leftPos + 89,
        topPos + 65,
        0,
        0,
        16,
        16,
        16,
        16);
    for (int i = 0; i < AdvancedRackBlockEntity.COMPARTMENT_COUNT; i++) {
      int x = leftPos + menu.slots.get(i).x;
      int y = topPos + menu.slots.get(i).y;
      graphics.fill(x, y, x + 16, y + 16, COMPARTMENT_BACKGROUND);
      if (!menu.slots.get(i).getItem().isEmpty() || menu.getFilter(i).isEmpty()) continue;
      SkewerOutlineRender.renderItem(
          graphics, menu.getFilter(i), x, y, SkewerGuiDecorator.colorFor(menu.getFilter(i)));
      graphics.fill(x, y, x + 16, y + 16, 0x66101418);
    }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0
        && isDepositAt(mouseX, mouseY)
        && minecraft != null
        && minecraft.gameMode != null) {
      minecraft.gameMode.handleInventoryButtonClick(
          menu.containerId, AdvancedRackMenu.DEPOSIT_BUTTON);
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTick);
    for (int i = 0; i < AdvancedRackBlockEntity.COMPARTMENT_COUNT; i++) drawCount(graphics, i);
    renderTooltip(graphics, mouseX, mouseY);
    int compartment = compartmentAt(mouseX, mouseY);
    if (compartment >= 0
        && menu.slots.get(compartment).getItem().isEmpty()
        && !menu.getFilter(compartment).isEmpty()) {
      graphics.renderTooltip(font, menu.getFilter(compartment), mouseX, mouseY);
    } else if (depositHovered) {
      graphics.renderTooltip(
          font, Component.translatable("gui.kaleidoscope_grilling.rack_deposit"), mouseX, mouseY);
    }
  }

  private int compartmentAt(double mouseX, double mouseY) {
    for (int i = 0; i < AdvancedRackBlockEntity.COMPARTMENT_COUNT; i++) {
      int x = leftPos + menu.slots.get(i).x;
      int y = topPos + menu.slots.get(i).y;
      if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) return i;
    }
    return -1;
  }

  private boolean isDepositAt(double mouseX, double mouseY) {
    return mouseX >= leftPos + 89
        && mouseX < leftPos + 105
        && mouseY >= topPos + 65
        && mouseY < topPos + 81;
  }

  private void drawCount(GuiGraphics graphics, int slot) {
    if (menu.getFilter(slot).isEmpty() && menu.slots.get(slot).getItem().isEmpty()) return;
    String text = Integer.toString(menu.slots.get(slot).getItem().getCount());
    int x = leftPos + menu.slots.get(slot).x + 16 - font.width(text);
    int y = topPos + menu.slots.get(slot).y + 8;
    graphics.drawString(font, text, x, y, 0xFFFFFFFF, true);
  }
}
