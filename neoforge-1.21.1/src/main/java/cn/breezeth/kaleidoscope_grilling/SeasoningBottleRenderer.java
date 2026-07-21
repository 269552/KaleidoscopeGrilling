package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SeasoningBottleRenderer
    implements BlockEntityRenderer<SeasoningBottleBlockEntity> {
  private static final float[][][] OFFSETS = {
    {{0, 0}},
    {{4, 3}, {-3, -1}},
    {{4, 4}, {-3, 3}, {3, -3.75F}},
    {{4, 5}, {-3, 4}, {3.5F, -3.25F}, {-4.25F, -3.25F}}
  };

  public SeasoningBottleRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      SeasoningBottleBlockEntity bottle,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    java.util.List<ItemStack> stacks = bottle.bottles();
    if (stacks.isEmpty()) return;
    pose.pushPose();
    pose.translate(0.5D, 0.5D, 0.5D);
    pose.mulPose(
        Axis.YP.rotationDegrees(
            180.0F - bottle.getBlockState().getValue(SeasoningBottleBlock.FACING).toYRot()));
    float[][] offsets = OFFSETS[stacks.size() - 1];
    for (int i = 0; i < stacks.size(); i++) {
      pose.pushPose();
      pose.translate(offsets[i][0] / 16.0F, 0, offsets[i][1] / 16.0F);
      Minecraft.getInstance()
          .getItemRenderer()
          .renderStatic(
              stacks.get(i),
              ItemDisplayContext.NONE,
              light,
              overlay,
              pose,
              buffers,
              bottle.getLevel(),
              i);
      pose.popPose();
    }
    pose.popPose();
  }
}
