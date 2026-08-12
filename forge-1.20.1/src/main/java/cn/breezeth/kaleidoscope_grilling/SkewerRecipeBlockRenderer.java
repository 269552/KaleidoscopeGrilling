package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public final class SkewerRecipeBlockRenderer
    implements BlockEntityRenderer<SkewerRecipeBlockEntity> {
  private static final float RECIPE_MODEL_DEPTH = 0.1F / 2.75F;
  private static final float PAGE_OFFSET = 0.3F / 16.0F;
  private static final float PAGE_ROTATION = -55.0F;

  private final BlockEntityRendererProvider.Context context;

  public SkewerRecipeBlockRenderer(BlockEntityRendererProvider.Context context) {
    this.context = context;
  }

  @Override
  public void render(
      SkewerRecipeBlockEntity recipe,
      float partialTick,
      PoseStack pose,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    ItemStack result = SkewerRecipeBookItem.readRecipeStack(recipe.recipeBook());
    if (result.isEmpty()) return;
    Direction facing = recipe.getBlockState().getValue(SkewerRecipeBlock.FACING);
    pose.pushPose();
    pose.translate(0.5D, 0.5D, 0.5D);
    pose.mulPose(Axis.YP.rotationDegrees(-facing.get2DDataValue() * 90.0F));
    pose.translate(-0.5D, -0.5D, -0.5D);
    ResourceLocation icon = SkewerGuiIconCache.recipeIcon16(result);
    if (icon != null) {
      renderIcon(pose, buffers, icon, light, overlay);
      pose.popPose();
      return;
    }
    pose.scale(0.5F, 0.5F, 0.5F);
    pose.translate(1.0D, 1.25D, 0.0D);
    // The FIXED transform turns a skewer's model Y axis into the wall normal.
    // Flatten that completed transform onto the recipe page and lift it enough to avoid z-fighting.
    pose.translate(0.0D, 0.0D, PAGE_OFFSET);
    pose.mulPose(Axis.ZP.rotationDegrees(PAGE_ROTATION));
    pose.scale(1.0F, 1.0F, RECIPE_MODEL_DEPTH);
    context
        .getItemRenderer()
        .renderStatic(
            result, ItemDisplayContext.FIXED, light, overlay, pose, buffers, recipe.getLevel(), 0);
    pose.popPose();
  }

  private static void renderIcon(
      PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, int light, int overlay) {
    float half = 0.24F;
    pose.translate(0.5F, 0.625F, PAGE_OFFSET);
    VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
    var p = pose.last();
    consumer.vertex(p.pose(), -half, -half, 0).color(255, 255, 255, 255).uv(0, 1)
        .overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
    consumer.vertex(p.pose(), half, -half, 0).color(255, 255, 255, 255).uv(1, 1)
        .overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
    consumer.vertex(p.pose(), half, half, 0).color(255, 255, 255, 255).uv(1, 0)
        .overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
    consumer.vertex(p.pose(), -half, half, 0).color(255, 255, 255, 255).uv(0, 0)
        .overlayCoords(overlay).uv2(light).normal(p.normal(), 0, 0, 1).endVertex();
  }
}
