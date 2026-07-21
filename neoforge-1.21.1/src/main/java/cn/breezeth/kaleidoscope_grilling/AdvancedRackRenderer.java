package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class AdvancedRackRenderer implements BlockEntityRenderer<AdvancedRackBlockEntity> {
  private static final double[] HOOK_X = {-0.284D, 0.0D, 0.278D};
  private final BlockEntityRendererProvider.Context context;

  public AdvancedRackRenderer(BlockEntityRendererProvider.Context context) {
    this.context = context;
  }

  @Override
  public void render(
      AdvancedRackBlockEntity rack,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    pose.pushPose();
    pose.translate(0.5D, 0.0D, 0.5D);
    pose.mulPose(
        Axis.YN.rotationDegrees(
            rack.getBlockState().getValue(AdvancedRackBlock.FACING).get2DDataValue() * 90.0F));

    int hook = 0;
    for (int slot = 5; slot < 9 && hook < HOOK_X.length; slot++) {
      ItemStack stack = rack.getItem(slot);
      if (stack.isEmpty()) continue;
      pose.pushPose();
      pose.translate(HOOK_X[hook], 0.0D, -0.30D);
      pose.scale(0.75F, 0.75F, 0.75F);
      pose.mulPose(Axis.XN.rotationDegrees(180.0F));
      pose.mulPose(Axis.YN.rotationDegrees(-25.0F));
      pose.mulPose(Axis.ZN.rotationDegrees(45.0F));
      context
          .getItemRenderer()
          .renderStatic(
              stack,
              ItemDisplayContext.FIXED,
              light,
              overlay,
              pose,
              buffers,
              rack.getLevel(),
              slot);
      pose.popPose();
      hook++;
    }
    pose.popPose();
  }
}
