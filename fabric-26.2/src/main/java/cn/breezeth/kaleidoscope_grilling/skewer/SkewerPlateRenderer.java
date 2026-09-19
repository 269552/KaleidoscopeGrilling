package cn.breezeth.kaleidoscope_grilling.skewer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SkewerPlateRenderer implements BlockEntityRenderer<SkewerPlateBlockEntity> {
  private static final float[][][] LAYOUTS = {
    {},
    {{8F, 4F, 6.3F, 0F}},
    {{5.6F, 4.15F, 5.95F, 0F}, {10.4F, 4.2F, 5.9F, 0F}},
    {{5.6F, 3.95F, 6.35F, 0F}, {10.4F, 4F, 6.3F, 0F}, {8F, 7.375F, 7.65F, -22.5F}},
    {
      {8F, 3.95F, 6.35F, 0F},
      {12.55F, 4F, 6.3F, 0F},
      {3.45F, 4F, 6.3F, 0F},
      {8F, 7.325F, 7.7F, -45F}
    },
    {
      {8F, 3.95F, 6.35F, 0F},
      {12.55F, 4F, 6.3F, 0F},
      {3.45F, 4F, 6.3F, 0F},
      {10.4F, 7.325F, 7.4F, -22.5F},
      {5.7F, 7.375F, 7.35F, -22.5F}
    }
  };

  public SkewerPlateRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      SkewerPlateBlockEntity plate,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    int facing = plate.getBlockState().getValue(SkewerPlateBlock.FACING).get2DDataValue();
    pose.pushPose();
    pose.translate(0.5, 0, 0.5);
    pose.mulPose(Axis.YP.rotationDegrees(-facing * 90F));
    pose.translate(-0.5, 0, -0.5);
    var skewers = plate.copySkewers();
    float[][] slots = slotsFor(skewers.size());
    for (int i = 0; i < skewers.size() && i < slots.length; i++) {
      ItemStack stack = skewers.get(i);
      float[] slot = slots[i];
      pose.pushPose();
      pose.translate(slot[0] / 16F, slot[1] / 16F, slot[2] / 16F);
      pose.mulPose(Axis.YP.rotationDegrees(slot[3]));
      // Fixed item models already contain a Z -180 display rotation; cancel it on plates.
      pose.mulPose(Axis.ZP.rotationDegrees(180F));
      pose.mulPose(Axis.XP.rotationDegrees(90F));
      // Render plate skewers at 1.2 times their authored model size.
      pose.scale(4F / 5F, 4F / 5F, 4F / 5F);
      Minecraft.getInstance()
          .getItemRenderer()
          .renderStatic(
              stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, plate.getLevel(), i);
      pose.popPose();
    }
    pose.popPose();
  }

  static float[][] slotsFor(int count) {
    return LAYOUTS[Math.max(0, Math.min(count, SkewerPlateBlockEntity.CAPACITY))];
  }
}
