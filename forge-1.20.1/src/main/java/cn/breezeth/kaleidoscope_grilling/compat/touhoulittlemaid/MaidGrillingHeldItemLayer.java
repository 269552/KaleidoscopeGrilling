package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityMaidRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class MaidGrillingHeldItemLayer extends RenderLayer<Mob, BedrockModel<Mob>> {
  private final ItemInHandRenderer itemRenderer;

  MaidGrillingHeldItemLayer(EntityMaidRenderer renderer, ItemInHandRenderer itemRenderer) {
    super(renderer);
    this.itemRenderer = itemRenderer;
  }

  @Override
  public void render(
      PoseStack poseStack, MultiBufferSource buffer, int light, Mob maid,
      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
      float netHeadYaw, float headPitch) {
    ItemStack stack = MaidGrillingAnimation.displayItem(maid);
    if (stack.isEmpty() || !getParentModel().hasRightArm()) return;
    poseStack.pushPose();
    getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
    if (getParentModel().hasArmPositioningModel(HumanoidArm.RIGHT)) {
      getParentModel().translateToPositioningHand(HumanoidArm.RIGHT, poseStack);
      poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.translate(0.0, 0.125, -0.0625);
    } else {
      poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      poseStack.translate(1.0 / 16.0, 0.125, -0.525);
    }
    itemRenderer.renderItem(
        maid, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, light);
    poseStack.popPose();
  }
}
