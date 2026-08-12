package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.GeoLayerRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntityRenderer;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class MaidGrillingGeckoHeldItemLayer<T extends Mob, R extends IGeoEntityRenderer<T>>
    extends GeoLayerRenderer<T, R> {
  private final ItemInHandRenderer itemRenderer;

  MaidGrillingGeckoHeldItemLayer(R renderer, ItemInHandRenderer itemRenderer) {
    super(renderer);
    this.itemRenderer = itemRenderer;
  }

  @Override
  public GeoLayerRenderer<T, R> copy(R renderer) {
    return new MaidGrillingGeckoHeldItemLayer<>(renderer, itemRenderer);
  }

  @Override
  public void render(
      PoseStack poseStack, MultiBufferSource buffer, int light, T maid,
      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
      float netHeadYaw, float headPitch) {
    ItemStack stack = MaidGrillingAnimation.displayItem(maid);
    ILocationModel model = getLocationModel(maid);
    if (stack.isEmpty() || model == null || model.rightHandBones().isEmpty()) return;
    poseStack.pushPose();
    if (!RenderUtils.prepMatrixForLocator(poseStack, model.rightHandBones())) {
      poseStack.translate(0.0, -0.0625, -0.1);
      poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
      itemRenderer.renderItem(
          maid, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, light);
    }
    poseStack.popPose();
  }
}
