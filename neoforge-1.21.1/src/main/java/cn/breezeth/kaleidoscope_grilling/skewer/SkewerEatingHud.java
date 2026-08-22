package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.client.SkewerAnimationDebug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Compact progress indicator for the custom multi-bite eating animation. */
@EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
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
  public static void render(RenderGuiLayerEvent.Post event) {
    if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;
    draw(event.getGuiGraphics());
  }

  private static void draw(GuiGraphics graphics) {
    Minecraft minecraft = Minecraft.getInstance();
    Player player = minecraft.player;
    if (player == null || minecraft.options.hideGui || !player.isUsingItem()) return;
    ItemStack stack = player.getUseItem();
    MultiBiteSkewerItem.AnimationProfile profile = SkewerRecipes.animationProfile(stack);
    if (profile == null) return;
    int duration = profile.duration();
    float elapsed = duration - player.getUseItemRemainingTicks() + minecraft.getTimer().getGameTimeDeltaPartialTick(true);
    float progress = Math.min(1.0F, Math.max(0.0F, elapsed / duration));
    boolean ready = elapsed >= MultiBiteSkewerItem.MINIMUM_EAT_TICKS;
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
          ready ? GREEN : YELLOW,
          x,
          y,
          0,
          0,
          fillWidth,
          HEIGHT,
          WIDTH,
          HEIGHT);
    ResourceLocation readyIcon = SkewerGuiIconCache.eatingHudIcon16(stack);
    if (readyIcon != null) {
      if (!ready) {
        // Keep the source icon's highlights and shadows while clearly showing it as pending.
        RenderSystem.setShaderColor(0.36F, 0.36F, 0.36F, 0.86F);
        graphics.blit(readyIcon, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
        graphics.blit(readyIcon, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
      }
    } else {
      graphics.blit(ready ? READY : PENDING, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
    if (SkewerAnimationDebug.isEnabled()) {
      String hand = player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
          ? "main hand"
          : "off hand";
      String debug = String.format(
          "%s  %.5fs  %s",
          SkewerAnimationDebug.animationName(), SkewerAnimationDebug.seconds(), hand);
      graphics.drawCenteredString(
          minecraft.font, debug, screenWidth / 2, y - 13, 0xFFFFFF55);
    }
  }

  private static ResourceLocation texture(String file) {
    return ResourceLocation.fromNamespaceAndPath(
        KaleidoscopeGrilling.MOD_ID, "textures/gui/" + file);
  }

  private SkewerEatingHud() {}
}
