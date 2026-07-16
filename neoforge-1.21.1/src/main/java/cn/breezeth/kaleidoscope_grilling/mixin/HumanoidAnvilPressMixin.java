package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.AnvilPressAnimation;
import cn.breezeth.kaleidoscope_grilling.AnvilPressAnimationAccess;
import cn.breezeth.kaleidoscope_grilling.ModItems;
import cn.breezeth.kaleidoscope_grilling.SeasoningAnimation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidAnvilPressMixin<T extends LivingEntity> {
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void grilling$pose(T entity, float walkPosition, float walkSpeed, float age,
                               float yaw, float pitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || !(entity instanceof AnvilPressAnimationAccess animation)) return;
        float partial = Mth.clamp(age - entity.tickCount, 0.0F, 1.0F);
        float press = animation.grilling$getAnvilPressProgress(partial);
        if (press >= 0.0F) {
            float slam = AnvilPressAnimation.slam(press), recovery = AnvilPressAnimation.recovery(press);
            float target = Mth.lerp(recovery, Mth.lerp(slam, -2.85F, -0.72F), rightArm.xRot);
            rightArm.xRot = leftArm.xRot = target;
            rightArm.yRot = -0.16F; leftArm.yRot = 0.16F;
            rightArm.zRot = 0.08F; leftArm.zRot = -0.08F;
            return;
        }
        ModelPart arm = player.getMainArm() == HumanoidArm.RIGHT ? rightArm : leftArm;
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND
                && player.getUseItem().is(ModItems.PENDING_SEASONING.get())) {
            float wave = Mth.sin(age * 1.7F);
            arm.xRot = -1.15F + wave * 0.12F;
            arm.yRot = side * (0.22F + wave * 0.18F);
            arm.zRot = side * (0.30F + wave * 0.25F);
            return;
        }
        float seasoning = animation.grilling$getSeasoningProgress(partial);
        if (seasoning >= 0.0F) {
            float arc = SeasoningAnimation.arc(seasoning);
            arm.xRot = -1.55F + 0.18F * arc;
            arm.yRot = side * (0.18F + 0.55F * Mth.sin(seasoning * Mth.TWO_PI * 2.0F));
            arm.zRot = side * (0.35F + 0.25F * arc);
        }
    }
}
