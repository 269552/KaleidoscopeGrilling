package cn.breezeth.kaleidoscope_grilling.rack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public final class RackSelectionScreen extends AbstractContainerScreen<RackSelectionMenu> {
  private int hovered = -1;
  private boolean depositHovered;
  private int ticksOpen;

  public RackSelectionScreen(RackSelectionMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    imageWidth = 176;
    imageHeight = 80;
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (!RackKeyHandler.isHeld()) {
      onClose();
      return;
    }
    ticksOpen++;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE61B2026);
    outline(graphics, leftPos, topPos, imageWidth, imageHeight, 0xFF69737D);
    graphics.fill(leftPos + 5, topPos + 17, leftPos + 171, topPos + 73, 0xA6101418);
    int pointer = at(mouseX, mouseY);
    hovered = pointer >= 0 && pointer < menu.rackCount() ? pointer : -1;
    depositHovered = isDepositAt(mouseX, mouseY);
    for (int i = 0; i < 9; i++) {
      int x = i < 5 ? leftPos + 42 + i * 18 : leftPos + 51 + (i - 5) * 18;
      int y = i < 5 ? topPos + 24 : topPos + 48;
      outline(graphics, x, y, 18, 18, i == hovered ? 0xFFD7A34A : 0xFF58616B);
    }
    drawAction(
        graphics,
        leftPos + 126,
        topPos + 45,
        42,
        20,
        Component.translatable("gui.kaleidoscope_grilling.rack_deposit_all"),
        depositHovered);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    graphics.drawString(font, title, 8, 6, 0xFFE8EDF2, false);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    float progress = easedProgress(partialTick);
    double logicalMouseX = logicalMouseX(mouseX, progress),
        logicalMouseY = logicalMouseY(mouseY, progress);
    graphics.pose().pushPose();
    graphics.pose().translate(width / 2F, height / 2F + (1 - progress) * 10, 0);
    float scale = .84F + .16F * progress;
    graphics.pose().scale(scale, scale, 1);
    graphics.pose().translate(-width / 2F, -height / 2F, 0);
    renderBg(
        graphics, partialTick, (int) Math.round(logicalMouseX), (int) Math.round(logicalMouseY));
    renderSlotItems(graphics);
    graphics.flush();
    drawRackNumbers(graphics);
    graphics.pose().popPose();
    if (progress > .9F) {
      int slot = at(logicalMouseX, logicalMouseY);
      if (slot >= 0 && slot < menu.rackCount())
        graphics.renderTooltip(font, menu.slots.get(slot).getItem(), mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    float progress = easedProgress(0);
    double logicalX = logicalMouseX(mouseX, progress), logicalY = logicalMouseY(mouseY, progress);
    if (button == 0 && isDepositAt(logicalX, logicalY)) {
      depositAll();
      onClose();
      return true;
    }
    int selected = at(logicalX, logicalY);
    if (button == 0 && selected >= 0 && selected < menu.rackCount()) {
      select(selected);
      return true;
    }
    return super.mouseClicked(logicalX, logicalY, button);
  }

  @Override
  public void mouseMoved(double mouseX, double mouseY) {
    float progress = easedProgress(0);
    double logicalX = logicalMouseX(mouseX, progress), logicalY = logicalMouseY(mouseY, progress);
    int pointer = at(logicalX, logicalY);
    hovered = pointer >= 0 && pointer < menu.rackCount() ? pointer : -1;
    depositHovered = isDepositAt(logicalX, logicalY);
    super.mouseMoved(logicalX, logicalY);
  }

  @Override
  public boolean keyPressed(int key, int scan, int modifiers) {
    if (minecraft != null)
      for (int i = 0; i < 9; i++) {
        if (minecraft.options.keyHotbarSlots[i].matches(key, scan) && i < menu.rackCount()) {
          select(i);
          return true;
        }
      }
    return super.keyPressed(key, scan, modifiers);
  }

  @Override
  public boolean keyReleased(int key, int scan, int modifiers) {
    if (RackKeyHandler.KEY.matches(key, scan)) {
      RackKeyHandler.finishHold();
      if (depositHovered) {
        depositAll();
        onClose();
      } else if (hovered >= 0 && hovered < menu.rackCount()) select(hovered);
      else onClose();
      return true;
    }
    return super.keyReleased(key, scan, modifiers);
  }

  private float easedProgress(float partialTick) {
    float p = Mth.clamp((ticksOpen + partialTick) / 9F, 0F, 1F);
    return 1F - (1F - p) * (1F - p) * (1F - p);
  }

  private double logicalMouseX(double mouseX, float progress) {
    float scale = .84F + progress * .16F;
    return width / 2.0 + (mouseX - width / 2.0) / scale;
  }

  private double logicalMouseY(double mouseY, float progress) {
    float scale = .84F + progress * .16F, offset = (1 - progress) * 10;
    return height / 2.0 + (mouseY - height / 2.0 - offset) / scale;
  }

  private void select(int slot) {
    if (minecraft != null && minecraft.gameMode != null)
      minecraft.gameMode.handleInventoryButtonClick(menu.containerId, slot);
  }

  private void depositAll() {
    if (minecraft != null && minecraft.gameMode != null)
      minecraft.gameMode.handleInventoryButtonClick(
          menu.containerId, RackSelectionMenu.DEPOSIT_ALL_BUTTON);
  }

  private int at(double mouseX, double mouseY) {
    for (int i = 0; i < 9; i++) {
      int x = i < 5 ? leftPos + 43 + i * 18 : leftPos + 52 + (i - 5) * 18,
          y = i < 5 ? topPos + 25 : topPos + 49;
      if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) return i;
    }
    return -1;
  }

  private boolean isDepositAt(double mouseX, double mouseY) {
    return mouseX >= leftPos + 126
        && mouseX < leftPos + 168
        && mouseY >= topPos + 45
        && mouseY < topPos + 65;
  }

  private void drawRackNumbers(GuiGraphics graphics) {
    graphics.pose().pushPose();
    graphics.pose().translate(0.0F, 0.0F, 200.0F);
    for (int i = 0; i < menu.rackCount(); i++) {
      int x = i < 5 ? leftPos + 42 + i * 18 : leftPos + 51 + (i - 5) * 18;
      int y = i < 5 ? topPos + 24 : topPos + 48;
      graphics.drawString(font, Integer.toString(i + 1), x + 11, y + 10, 0xFFFFFFFF, true);
    }
    graphics.pose().popPose();
  }

  private void renderSlotItems(GuiGraphics graphics) {
    for (int i = 0; i < menu.rackCount(); i++) {
      var slot = menu.slots.get(i);
      if (!slot.getItem().isEmpty())
        graphics.renderItem(slot.getItem(), leftPos + slot.x, topPos + slot.y);
    }
  }

  private void drawAction(
      GuiGraphics g, int x, int y, int w, int h, Component text, boolean active) {
    g.fill(x, y, x + w, y + h, active ? 0xCC38434B : 0xA822292F);
    outline(g, x, y, w, h, active ? 0xFFD7A34A : 0xFF66717A);
    g.drawCenteredString(font, text, x + w / 2, y + 6, active ? 0xFFFFFFFF : 0xFFD5DBDF);
  }

  private static void outline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
    graphics.fill(x, y, x + w, y + 1, color);
    graphics.fill(x, y + h - 1, x + w, y + h, color);
    graphics.fill(x, y, x + 1, y + h, color);
    graphics.fill(x + w - 1, y, x + w, y + h, color);
  }
}
