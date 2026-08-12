package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Compact progress indicator for the custom multi-bite eating animation. */
@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class SkewerEatingHud {
  private static final int WIDTH = 102;
  private static final int HEIGHT = 5;
  private static final int ICON_SIZE = 16;
  private static final ResourceLocation BASE = texture("skewer_eating_base.png");
  private static final ResourceLocation YELLOW = texture("skewer_eating_progress_yellow.png");
  private static final ResourceLocation GREEN = texture("skewer_eating_progress_green.png");
  private static final ResourceLocation PENDING = texture("skewer_eating_pending.png");
  private static final ResourceLocation READY = texture("skewer_eating_ready_fallback.png");

  @SubscribeEvent
  public static void render(RenderGuiOverlayEvent.Post event) {
    if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
    draw(event.getGuiGraphics());
  }

  private static void draw(GuiGraphics graphics) {
    Minecraft minecraft = Minecraft.getInstance();
    Player player = minecraft.player;
    if (player == null || minecraft.options.hideGui || !player.isUsingItem()) return;
    ItemStack stack = player.getUseItem();
    if (!(stack.getItem() instanceof MultiBiteSkewerItem skewer)) return;

    int duration = skewer.getUseDuration(stack);
    float elapsed = duration - player.getUseItemRemainingTicks() + minecraft.getPartialTick();
    float progress = Math.min(1.0F, Math.max(0.0F, elapsed / duration));
    boolean committed = elapsed >= MultiBiteSkewerItem.MINIMUM_EAT_TICKS;
    int screenWidth = minecraft.getWindow().getGuiScaledWidth();
    int screenHeight = minecraft.getWindow().getGuiScaledHeight();
    int x = screenWidth / 2 + 50 - WIDTH / 2;
    int y = screenHeight - 54;
    int fillWidth = Math.min(WIDTH, Math.max(0, Math.round(WIDTH * progress)));
    int threshold =
        x + Math.round(WIDTH * MultiBiteSkewerItem.MINIMUM_EAT_TICKS / (float) duration);
    int iconX = threshold - ICON_SIZE / 2;
    int iconY = y - (ICON_SIZE - HEIGHT) / 2;

    graphics.blit(BASE, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    if (fillWidth > 0)
      graphics.blit(
          committed ? GREEN : YELLOW,
          x,
          y,
          0,
          0,
          fillWidth,
          HEIGHT,
          WIDTH,
          HEIGHT);
    ResourceLocation readyIcon = committed ? SkewerGuiIconCache.eatingHudIcon16(stack) : null;
    graphics.blit(
        committed && readyIcon != null ? readyIcon : committed ? READY : PENDING,
        iconX,
        iconY,
        0,
        0,
        ICON_SIZE,
        ICON_SIZE,
        ICON_SIZE,
        ICON_SIZE);
  }

  private static ResourceLocation texture(String file) {
    return new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "textures/gui/" + file);
  }

  private SkewerEatingHud() {}
}
