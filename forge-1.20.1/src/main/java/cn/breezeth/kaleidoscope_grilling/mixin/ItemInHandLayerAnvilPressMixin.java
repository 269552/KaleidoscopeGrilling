package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.AnvilPressAnimation;
import cn.breezeth.kaleidoscope_grilling.AnvilPressAnimationAccess;
import cn.breezeth.kaleidoscope_grilling.EnderPearlEatingAnimation;
import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.ModItems;
import cn.breezeth.kaleidoscope_grilling.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.OilBrushAnimation;
import cn.breezeth.kaleidoscope_grilling.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.OilPressTools;
import cn.breezeth.kaleidoscope_grilling.SkewerEatingAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerAnvilPressMixin {
  private static final ResourceLocation ENDER_PEARL_PIECE =
      new ResourceLocation(
          KaleidoscopeGrilling.MOD_ID, "item/fixed_skewers/ender_pearl_bite_piece");

  private static final String RENDER_ITEM =
      "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

  @Inject(
      method = "renderArmWithItem",
      at = @At(value = "INVOKE", target = RENDER_ITEM, shift = At.Shift.BEFORE))
  private void grilling$invertSeasoningBefore(
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext context,
      HumanoidArm arm,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight,
      CallbackInfo ci) {
    if (!grilling$isPouring(entity, stack, arm)) return;
    pose.pushPose();
    pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
  }

  @Inject(
      method = "renderArmWithItem",
      at = @At(value = "INVOKE", target = RENDER_ITEM, shift = At.Shift.AFTER))
  private void grilling$invertSeasoningAfter(
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext context,
      HumanoidArm arm,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight,
      CallbackInfo ci) {
    if (grilling$isPouring(entity, stack, arm)) pose.popPose();
  }

  private static boolean grilling$isPouring(LivingEntity entity, ItemStack stack, HumanoidArm arm) {
    return entity instanceof Player player
        && arm == player.getMainArm()
        && stack.is(ModItems.SPECIAL_SEASONING.get())
        && player instanceof AnvilPressAnimationAccess animation
        && animation.grilling$getSeasoningProgress(Minecraft.getInstance().getPartialTick())
            >= 0.0F;
  }

  @Inject(
      method = "renderArmWithItem",
      at = @At(value = "INVOKE", target = RENDER_ITEM, shift = At.Shift.BEFORE))
  private void grilling$swingOilBrushBefore(
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext context,
      HumanoidArm arm,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight,
      CallbackInfo ci) {
    float progress = grilling$oilBrushProgress(entity, stack, arm);
    if (progress < 0.0F) return;
    pose.pushPose();
    float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
    pose.mulPose(Axis.ZP.rotationDegrees(side * OilBrushAnimation.swing(progress) * 16.0F));
  }

  @Inject(
      method = "renderArmWithItem",
      at = @At(value = "INVOKE", target = RENDER_ITEM, shift = At.Shift.AFTER))
  private void grilling$swingOilBrushAfter(
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext context,
      HumanoidArm arm,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight,
      CallbackInfo ci) {
    if (grilling$oilBrushProgress(entity, stack, arm) >= 0.0F) pose.popPose();
  }

  private static float grilling$oilBrushProgress(
      LivingEntity entity, ItemStack stack, HumanoidArm arm) {
    if (!(entity instanceof Player player)
        || !OilPotCompat.isOilPot(stack)
        || !(player instanceof AnvilPressAnimationAccess animation)) return -1.0F;
    HumanoidArm activeArm =
        animation.grilling$getOilBrushHand() == net.minecraft.world.InteractionHand.MAIN_HAND
            ? player.getMainArm()
            : player.getMainArm().getOpposite();
    return arm == activeArm
        ? animation.grilling$getOilBrushProgress(Minecraft.getInstance().getPartialTick())
        : -1.0F;
  }

  @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
  private void grilling$renderIndependentTool(
      LivingEntity entity,
      ItemStack stack,
      ItemDisplayContext context,
      HumanoidArm arm,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight,
      CallbackInfo ci) {
    if (grilling$renderEatingSkewer(
        entity, stack, arm, pose, buffers, packedLight)) {
      ci.cancel();
      return;
    }
    if (!(entity instanceof Player player)
        || arm != player.getMainArm()
        || OilPressTools.progress(stack) <= 0
        || !(player instanceof AnvilPressAnimationAccess animation)) return;
    float partialTick = Minecraft.getInstance().getPartialTick();
    float progress = animation.grilling$getAnvilPressProgress(partialTick);
    if (progress < 0.0F) return;

    float slam = AnvilPressAnimation.slam(progress);
    float recovery = AnvilPressAnimation.recovery(progress);
    float side = arm == HumanoidArm.RIGHT ? -1.0F : 1.0F;
    pose.pushPose();
    pose.translate(
        Mth.lerp(recovery, 0.0F, side * 0.34F),
        Mth.lerp(recovery, -0.72F + 1.18F * slam, 0.54F),
        Mth.lerp(recovery, -0.12F - 0.42F * slam, -0.12F));
    pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(recovery, 8.0F + 82.0F * slam, 0.0F)));
    pose.mulPose(Axis.YP.rotationDegrees(180.0F));
    float scale = Mth.lerp(recovery, 1.35F, 1.0F);
    pose.scale(scale, scale, scale);
    Minecraft.getInstance()
        .getItemRenderer()
        .renderStatic(
            stack,
            ItemDisplayContext.FIXED,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            pose,
            buffers,
            entity.level(),
            entity.getId());
    pose.popPose();
    ci.cancel();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private boolean grilling$renderEatingSkewer(
      LivingEntity entity,
      ItemStack stack,
      HumanoidArm arm,
      PoseStack pose,
      MultiBufferSource buffers,
      int packedLight) {
    if (!(entity instanceof Player player)
        || !player.isUsingItem()
        || !(player.getUseItem().getItem() instanceof MultiBiteSkewerItem animated)) return false;

    HumanoidArm activeArm =
        player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
            ? player.getMainArm()
            : player.getMainArm().getOpposite();
    if (animated.uses(MultiBiteSkewerItem.AnimationProfile.RAW_ENDER_PEARL)) {
      EnderPearlEatingAnimation.Pose animation =
          EnderPearlEatingAnimation.sample(player, Minecraft.getInstance().getPartialTick());
      ArmedModel model =
          (ArmedModel) ((ItemInHandLayer) (Object) this).getParentModel();
      pose.pushPose();
      model.translateToHand(arm, pose);
      if (activeArm == arm) {
        EnderPearlEatingAnimation.transformMainItem(
            pose, arm, animation.mainItem());
        Minecraft.getInstance()
            .getItemRenderer()
            .renderStatic(
                entity,
                player.getUseItem(),
                ItemDisplayContext.NONE,
                arm == HumanoidArm.LEFT,
                pose,
                buffers,
                entity.level(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                entity.getId());
      } else if (animation.secondItem().scale() > 0.001F) {
        EnderPearlEatingAnimation.transformSecondItem(
            pose, arm, animation.secondItem());
        Minecraft.getInstance()
            .getItemRenderer()
            .render(
                player.getUseItem(),
                ItemDisplayContext.NONE,
                arm == HumanoidArm.LEFT,
                pose,
                buffers,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                Minecraft.getInstance().getModelManager().getModel(ENDER_PEARL_PIECE));
      }
      pose.popPose();
      return true;
    }
    if (activeArm != arm || !(stack.getItem() instanceof MultiBiteSkewerItem)) return false;

    SkewerEatingAnimation.ArmPose animation =
        SkewerEatingAnimation.sample(
            player, Minecraft.getInstance().getPartialTick(), animated.animationProfile());
    ArmedModel model =
        (ArmedModel) ((ItemInHandLayer) (Object) this).getParentModel();

    pose.pushPose();
    model.translateToHand(arm, pose);
    SkewerEatingAnimation.transformItem(pose, arm, animation);
    Minecraft.getInstance()
        .getItemRenderer()
        .renderStatic(
            entity,
            stack,
            ItemDisplayContext.NONE,
            arm == HumanoidArm.LEFT,
            pose,
            buffers,
            entity.level(),
            packedLight,
            OverlayTexture.NO_OVERLAY,
            entity.getId());
    pose.popPose();
    return true;
  }
}
