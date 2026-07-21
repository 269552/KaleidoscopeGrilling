package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class AnvilPressFirstPersonAnimation {
  @SubscribeEvent
  public static void render(RenderHandEvent event) {
    Minecraft minecraft = Minecraft.getInstance();
    var player = minecraft.player;
    if (!(player instanceof AnvilPressAnimationAccess animation)) return;
    float progress = animation.grilling$getAnvilPressProgress(event.getPartialTick());
    if (progress < 0.0F || OilPressTools.progress(player.getMainHandItem()) <= 0) return;

    event.setCanceled(true);
    if (event.getHand() != InteractionHand.MAIN_HAND) return;

    float slam = AnvilPressAnimation.slam(progress);
    float recovery = AnvilPressAnimation.recovery(progress);
    renderTool(event, player.getMainHandItem(), slam, recovery);
  }

  private static void renderTool(
      RenderHandEvent event, ItemStack tool, float slam, float recovery) {
    Minecraft minecraft = Minecraft.getInstance();
    PoseStack pose = event.getPoseStack();
    float x = Mth.lerp(recovery, 0.0F, 0.48F);
    float y = Mth.lerp(recovery, -1.08F + 0.82F * slam, -0.52F);
    float z = Mth.lerp(recovery, -0.48F - 0.48F * slam, -0.72F);
    float xRot = Mth.lerp(recovery, -72.0F + 108.0F * slam, 0.0F);

    pose.pushPose();
    pose.translate(x, y, z);
    pose.mulPose(Axis.XP.rotationDegrees(xRot));
    pose.mulPose(Axis.YP.rotationDegrees(180.0F));
    float scale = Mth.lerp(recovery, 1.35F, 1.0F);
    pose.scale(scale, scale, scale);
    minecraft
        .getItemRenderer()
        .renderStatic(
            tool,
            ItemDisplayContext.FIXED,
            event.getPackedLight(),
            OverlayTexture.NO_OVERLAY,
            pose,
            event.getMultiBufferSource(),
            minecraft.level,
            minecraft.player.getId());
    pose.popPose();
  }

  private AnvilPressFirstPersonAnimation() {}
}
