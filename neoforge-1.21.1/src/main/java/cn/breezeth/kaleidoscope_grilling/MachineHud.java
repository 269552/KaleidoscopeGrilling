package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class MachineHud {
  private static final int WIDTH = 194;

  @SubscribeEvent
  public static void render(RenderGuiLayerEvent.Post event) {
    if (!HudControl.isEnabled()) return;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null
        || minecraft.options.hideGui
        || !(minecraft.hitResult instanceof BlockHitResult hit)) return;
    Object blockEntity = minecraft.level.getBlockEntity(hit.getBlockPos());
    if (blockEntity instanceof OilPressBlockEntity press)
      drawPress(event.getGuiGraphics(), hit.getBlockPos(), press);
    else if (blockEntity instanceof GrillBlockEntity grill)
      drawGrill(event.getGuiGraphics(), grill);
    else if (blockEntity instanceof BigVatBlockEntity vat) drawVat(event.getGuiGraphics(), vat);
  }

  private static void drawPress(GuiGraphics graphics, BlockPos pos, OilPressBlockEntity press) {
    Minecraft minecraft = Minecraft.getInstance();
    int height = 126, x = OilPotHud.panelX(minecraft, WIDTH), y = centerY(minecraft, height);
    OilPotHud.panel(graphics, x, y, WIDTH, height, 0xFFB9A06A);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.press.title"),
        x + 12,
        y + 9,
        0xFFF3E9D2,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.press.cakes", press.cakes(), OilPressBlockEntity.MAX_CAKES),
        x + 12,
        y + 23,
        0xFFE5E8E8,
        false);
    for (int i = 0; i < press.cakes(); i++)
      graphics.renderItem(new ItemStack(ModItems.OIL_CAKE.get()), x + 12 + i * 19, y + 35);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.press.progress",
            press.progress(),
            OilPressBlockEntity.REQUIRED_PROGRESS),
        x + 12,
        y + 56,
        0xFFAAB7B8,
        false);
    bar(
        graphics,
        x + 12,
        y + 68,
        WIDTH - 24,
        press.progress(),
        OilPressBlockEntity.REQUIRED_PROGRESS,
        0xFFB9A06A);
    OilPressContainerApi.TransferResult container =
        OilPressContainerApi.probeNearby(minecraft.level, pos, OilPressBlockEntity.MAX_CAKES);
    BigVatBlockEntity vat =
        container.containerPos() != null
                && minecraft.level.getBlockEntity(container.containerPos())
                    instanceof BigVatBlockEntity found
            ? found
            : null;
    Component vatState;
    int vatColor;
    if (container.status() == OilPressContainerApi.TransferStatus.NO_CONTAINER) {
      vatState = Component.translatable("hud.kaleidoscope_grilling.press.vat.none");
      vatColor = 0xFFE68B73;
    } else if (container.status() == OilPressContainerApi.TransferStatus.INCOMPATIBLE) {
      vatState = Component.translatable("hud.kaleidoscope_grilling.press.vat.wrong");
      vatColor = 0xFFE68B73;
    } else if (container.status() == OilPressContainerApi.TransferStatus.FULL) {
      vatState =
          vat == null
              ? Component.translatable("hud.kaleidoscope_grilling.press.vat.compatible_full")
              : Component.translatable(
                  "hud.kaleidoscope_grilling.press.vat.full",
                  vat.buckets(),
                  BigVatBlockEntity.CAPACITY_BUCKETS);
      vatColor = 0xFFE68B73;
    } else if (vat != null) {
      vatState =
          Component.translatable(
              "hud.kaleidoscope_grilling.press.vat.found",
              vat.buckets(),
              BigVatBlockEntity.CAPACITY_BUCKETS);
      vatColor = 0xFFB8D8CD;
    } else {
      vatState =
          Component.translatable(
              "hud.kaleidoscope_grilling.press.vat.compatible", container.handlerId());
      vatColor = 0xFFB8D8CD;
    }
    graphics.drawString(minecraft.font, vatState, x + 12, y + 79, vatColor, false);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.press.output", press.cakes(), press.cakes()),
        x + 12,
        y + 92,
        0xFFE0A83B,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.press.tools"),
        x + 12,
        y + 107,
        0xFF8D9697,
        false);
  }

  private static void drawGrill(GuiGraphics graphics, GrillBlockEntity grill) {
    Minecraft minecraft = Minecraft.getInstance();
    int height = 94, x = OilPotHud.panelX(minecraft, WIDTH), y = centerY(minecraft, height);
    OilPotHud.panel(graphics, x, y, WIDTH, height, 0xFFE05A32);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.grill.title"),
        x + 12,
        y + 9,
        0xFFF3E9D2,
        false);
    if (grill.getPhase() == 2 && grill.isSeasoned())
      graphics.drawString(
          minecraft.font,
          Component.translatable("message.kaleidoscope_grilling.grill_ready_to_take"),
          x + 12,
          y + 22,
          0xFFB8D8CD,
          false);
    if (!grill.isEmpty()) {
      int max = grill.getPhase() <= 2 ? 800 : 400;
      graphics.drawString(
          minecraft.font,
          Component.translatable(
              "hud.kaleidoscope_grilling.grill.timer", Math.min(grill.getPhaseTicks(), max), max),
          x + 12,
          y + 37,
          0xFFAAB7B8,
          false);
      bar(graphics, x + 12, y + 49, WIDTH - 24, grill.getPhaseTicks(), max, 0xFFE05A32);
    }
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.grill.flips", grill.getFlips(), 4),
        x + 12,
        y + 63,
        grill.getFlips() >= 4 ? 0xFFB8D8CD : 0xFFE6B873,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.grill.seasoning." + (grill.isSeasoned() ? "added" : "none")),
        x + 12,
        y + 77,
        grill.isSeasoned() ? 0xFFE0A83B : 0xFF8D9697,
        false);
  }

  private static void drawVat(GuiGraphics graphics, BigVatBlockEntity vat) {
    Minecraft minecraft = Minecraft.getInstance();
    int height = 84, x = OilPotHud.panelX(minecraft, WIDTH), y = centerY(minecraft, height);
    int accent =
        vat.fluid().isEmpty()
            ? 0xFFE0A83B
            : vat.fluid().getFluid() == Fluids.WATER
                ? 0xFF5AA6D6
                : vat.fluid().getFluid() == Fluids.LAVA ? 0xFFFF6A22 : 0xFFE0A83B;
    OilPotHud.panel(graphics, x, y, WIDTH, height, accent);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.vat.title"),
        x + 12,
        y + 9,
        0xFFF3E9D2,
        false);
    graphics.drawString(
        minecraft.font,
        vat.fluid().isEmpty()
            ? Component.translatable("hud.kaleidoscope_grilling.vat.content.empty")
            : vat.fluid().getHoverName(),
        x + 12,
        y + 24,
        0xFFE5E8E8,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.vat.capacity",
            vat.amount() / (float) BigVatBlockEntity.BUCKET_VOLUME,
            BigVatBlockEntity.CAPACITY_BUCKETS),
        x + 12,
        y + 39,
        0xFFAAB7B8,
        false);
    bar(graphics, x + 12, y + 54, WIDTH - 24, vat.amount(), BigVatBlockEntity.CAPACITY, accent);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.vat.accepts"),
        x + 12,
        y + 66,
        0xFF8D9697,
        false);
  }

  private static void bar(
      GuiGraphics graphics, int x, int y, int width, int value, int max, int color) {
    graphics.fill(x, y, x + width, y + 5, 0xFF303B3D);
    graphics.fill(
        x, y, x + width * Math.max(0, Math.min(value, max)) / Math.max(1, max), y + 5, color);
  }

  private static int centerY(Minecraft minecraft, int height) {
    return Math.max(8, minecraft.getWindow().getGuiScaledHeight() / 2 - height / 2);
  }

  private MachineHud() {}
}
