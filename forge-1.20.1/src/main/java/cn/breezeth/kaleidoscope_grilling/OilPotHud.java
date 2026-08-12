package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class OilPotHud {
  private static final int WIDTH = 174;
  private static final int HEIGHT = 68;

  @SubscribeEvent
  public static void render(RenderGuiOverlayEvent.Post event) {
    if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
    if (!HudControl.isEnabled()) return;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null
        || minecraft.options.hideGui
        || !(minecraft.hitResult instanceof BlockHitResult hit)
        || !(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof TypedOilPotAccess oil))
      return;
    draw(event.getGuiGraphics(), oil);
  }

  private static void draw(GuiGraphics graphics, TypedOilPotAccess oil) {
    Minecraft minecraft = Minecraft.getInstance();
    String type = oil.grilling$getOilType();
    int count = oil.grilling$getOilCount();
    int capacity = OilPotCompat.capacity(type);
    String state = type.isEmpty() ? (count > 0 ? "fat" : "empty") : type;
    int accent =
        switch (state) {
          case "canola" -> 0xFFC08A24;
          case "secret_chili" -> 0xFFE05A32;
          case "premium_chili" -> 0xFFB52828;
          case "fat" -> 0xFFC7C9C8;
          default -> 0xFFE0A83B;
        };
    int x = panelX(minecraft, WIDTH);
    int y = minecraft.getWindow().getGuiScaledHeight() / 2 - HEIGHT / 2;
    panel(graphics, x, y, WIDTH, HEIGHT, accent);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.oil_pot.title"),
        x + 12,
        y + 9,
        0xFFF3E9D2,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable("tooltip.kaleidoscope_grilling.oil_pot." + state, count),
        x + 12,
        y + 23,
        0xFFE5E8E8,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.oil_pot.capacity", count, capacity, capacity - count),
        x + 12,
        y + 36,
        0xFFAAB7B8,
        false);
    graphics.fill(x + 12, y + 52, x + WIDTH - 12, y + 57, 0xFF303B3D);
    graphics.fill(
        x + 12, y + 52, x + 12 + (WIDTH - 24) * count / capacity, y + 57, accent);
  }

  static int panelX(Minecraft minecraft, int width) {
    int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 24;
    return Math.min(x, minecraft.getWindow().getGuiScaledWidth() - width - 8);
  }

  static void panel(GuiGraphics graphics, int x, int y, int width, int height, int accent) {
    graphics.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x55000000);
    graphics.fill(x, y, x + width, y + height, 0xE6121718);
    graphics.fill(x, y, x + 3, y + height, accent);
    graphics.fill(x + 3, y + 29, x + width, y + 30, 0x554F6265);
  }

  private OilPotHud() {}
}
