package cn.breezeth.kaleidoscope_grilling.skewer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class SkewerEatingAnimation {
  public static final float LENGTH_SECONDS = 4.5F;

  private static final float[] POSITION_TIMES = {
    0.0F, 0.45833F, 0.54167F, 0.58333F, 0.95833F, 1.20833F, 1.75F, 1.83333F,
    1.91667F, 1.95833F, 2.33333F, 2.58333F, 2.875F, 3.04167F, 3.08333F,
    3.45833F, 3.625F, 3.83333F, 3.875F, 4.08333F, 4.33333F, 4.5F
  };
  private static final float[] POSITION_X = {
    3.0F, 8.0F, 8.0F, 6.31F, 5.0F, 8.0F, 8.0F, 8.0F, 8.0F, 6.31F, 5.0F,
    8.0F, 8.0F, 8.0F, 6.31F, 5.0F, 10.0F, 8.0F, 6.5F, 5.0F, 12.0F, 10.0F
  };
  private static final float[] POSITION_Y = {
    -6.0F, -9.0F, -6.0F, -5.81F, -6.0F, -6.0F, -9.0F, -9.0F, -6.0F,
    -5.81F, -6.0F, -5.0F, -6.0F, -5.0F, -4.94F, -5.0F, -5.0F, -5.0F,
    -5.0F, -5.0F, -5.0F, -11.0F
  };
  private static final float[] POSITION_Z = {
    -5.0F, -9.0F, -7.0F, -6.06F, -5.0F, -2.0F, -9.0F, -9.0F, -7.0F,
    -6.06F, -5.0F, -2.0F, -7.0F, -7.0F, -5.87F, -5.0F, -5.0F, -7.0F,
    -6.31F, -5.0F, -5.0F, -5.0F
  };

  private static final float[] ROTATION_TIMES = {
    0.0F, 0.45833F, 0.54167F, 0.95833F, 1.20833F, 1.75F, 1.83333F, 1.91667F,
    2.33333F, 2.58333F, 2.875F, 2.95833F, 3.04167F, 3.45833F, 3.625F,
    3.83333F, 4.08333F, 4.33333F, 4.5F
  };
  private static final float[] ROTATION_X = {
    83.7983F, 114.0944F, 121.5944F, 127.2944F, 101.5944F, 114.0944F,
    114.0944F, 121.5944F, 127.2944F, 101.5944F, 114.0944F, 114.0944F,
    121.5944F, 125.5353F, 126.3407F, 121.5944F, 125.966F, 116.3981F, 119.9402F
  };
  private static final float[] ROTATION_Y = {
    9.8513F, -3.7751F, -3.7751F, -3.7751F, -3.7751F, -3.7751F, -3.7751F,
    -3.7751F, -3.7751F, -3.7751F, -3.7751F, -3.7751F, -3.7751F, -12.7136F,
    -9.7778F, -3.7751F, -11.2528F, -16.9896F, -8.674F
  };
  private static final float[] ROTATION_Z = {
    16.6457F, 53.8578F, 53.8578F, 53.8578F, 53.8578F, 53.8578F, 53.8578F,
    53.8578F, 53.8578F, 53.8578F, 53.8578F, 53.8578F, 53.8578F, 66.0431F,
    61.916F, 53.8578F, 63.969F, 72.4197F, 56.6086F
  };

  private static final float[] ITEM_ROTATION_TIMES = {
    0.45833F, 0.95833F, 1.20833F, 2.33333F, 2.58333F, 3.45833F, 3.70833F,
    4.08333F
  };
  private static final float[] ITEM_ROTATION_X = {
    0.0F, 12.5F, 0.0F, 12.5F, 0.0F, 12.5F, 0.0F, 12.5F
  };

  private static final float[] SQUID_POSITION_TIMES = {
    0.0F, 0.45833F, 0.54167F, 0.58333F, 0.95833F, 1.20833F, 1.41667F,
    1.58333F, 1.625F, 2.0F, 2.29167F, 2.58333F, 2.70833F, 3.25F,
    3.54167F, 3.83333F, 3.95833F, 4.41667F
  };
  private static final float[] SQUID_POSITION_X = {
    3.0F, 8.0F, 8.0F, 6.31F, 5.0F, 8.0F, 8.0F, 8.0F, 6.31F, 5.0F,
    10.0F, 8.0F, 6.5F, 5.0F, 10.0F, 8.0F, 6.5F, 18.5F
  };
  private static final float[] SQUID_POSITION_Y = {
    -6.0F, -9.0F, -6.0F, -5.81F, -6.0F, -6.0F, -6.0F, -5.0F, -4.94F,
    -5.0F, -5.0F, -5.0F, -5.0F, -5.0F, -5.0F, -5.0F, -5.0F, -13.0F
  };
  private static final float[] SQUID_POSITION_Z = {
    -5.0F, -9.0F, -7.0F, -6.06F, -5.0F, -2.0F, -7.0F, -7.0F, -5.87F,
    -5.0F, -5.0F, -7.0F, -6.31F, -5.0F, -5.0F, -7.0F, -6.31F, -6.31F
  };
  private static final float[] SQUID_ROTATION_TIMES = {
    0.0F, 0.45833F, 0.54167F, 0.95833F, 1.20833F, 1.41667F, 1.5F,
    1.58333F, 2.0F, 2.29167F, 2.58333F, 3.25F, 3.54167F, 3.83333F
  };
  private static final float[] SQUID_ROTATION_X = {
    83.7983F, 114.0944F, 121.5944F, 127.2944F, 101.5944F, 114.0944F,
    114.0944F, 121.5944F, 125.5353F, 126.3407F, 121.5944F, 125.5353F,
    126.3407F, 121.5944F
  };
  private static final float[] SQUID_ROTATION_Y = {
    9.8513F, -3.7751F, -3.7751F, -3.7751F, -3.7751F, -3.7751F, -3.7751F,
    -3.7751F, -12.7136F, -9.7778F, -3.7751F, -12.7136F, -9.7778F, -3.7751F
  };
  private static final float[] SQUID_ROTATION_Z = {
    16.6457F, 53.8578F, 53.8578F, 53.8578F, 53.8578F, 53.8578F, 53.8578F,
    53.8578F, 66.0431F, 61.916F, 53.8578F, 66.0431F, 61.916F, 53.8578F
  };
  private static final float[] SQUID_ITEM_ROTATION_TIMES = {
    0.45833F, 0.95833F, 1.20833F, 2.33333F, 2.58333F, 3.66667F,
    3.91667F, 3.95833F, 4.41667F
  };
  private static final float[] SQUID_ITEM_ROTATION_X = {
    0.0F, 12.5F, 0.0F, 20.0F, 0.0F, 12.5F, 0.0F, 10.21F, 12.5F
  };
  private static final float[] SQUID_ITEM_POSITION_TIMES = {0.0F, 2.33333F};
  private static final float[] SQUID_ITEM_POSITION_Y = {0.0F, -0.75F};

  private SkewerEatingAnimation() {}

  public static ArmPose sample(Player player, float partialTick) {
    return sample(player, partialTick, MultiBiteSkewerItem.AnimationProfile.BEEF);
  }

  public static ArmPose sample(
      Player player, float partialTick, MultiBiteSkewerItem.AnimationProfile profile) {
    float seconds =
        Mth.clamp(
            (player.getUseItem().getUseDuration()
                    - player.getUseItemRemainingTicks()
                    + partialTick)
                / 20.0F,
            0.0F,
            LENGTH_SECONDS);
    return profile == MultiBiteSkewerItem.AnimationProfile.SQUID_TENTACLE
        ? sampleSquid(seconds)
        : sample(seconds);
  }

  public static ArmPose sample(float seconds) {
    float time = Mth.clamp(seconds, 0.0F, LENGTH_SECONDS);
    return new ArmPose(
        catmullRom(time, POSITION_TIMES, POSITION_X),
        catmullRom(time, POSITION_TIMES, POSITION_Y),
        catmullRom(time, POSITION_TIMES, POSITION_Z),
        catmullRom(time, ROTATION_TIMES, ROTATION_X),
        catmullRom(time, ROTATION_TIMES, ROTATION_Y),
        catmullRom(time, ROTATION_TIMES, ROTATION_Z),
        0.0F,
        catmullRom(time, ITEM_ROTATION_TIMES, ITEM_ROTATION_X));
  }

  private static ArmPose sampleSquid(float seconds) {
    float time = Mth.clamp(seconds, 0.0F, LENGTH_SECONDS);
    return new ArmPose(
        catmullRom(time, SQUID_POSITION_TIMES, SQUID_POSITION_X),
        catmullRom(time, SQUID_POSITION_TIMES, SQUID_POSITION_Y),
        catmullRom(time, SQUID_POSITION_TIMES, SQUID_POSITION_Z),
        catmullRom(time, SQUID_ROTATION_TIMES, SQUID_ROTATION_X),
        catmullRom(time, SQUID_ROTATION_TIMES, SQUID_ROTATION_Y),
        catmullRom(time, SQUID_ROTATION_TIMES, SQUID_ROTATION_Z),
        catmullRom(time, SQUID_ITEM_POSITION_TIMES, SQUID_ITEM_POSITION_Y),
        catmullRom(time, SQUID_ITEM_ROTATION_TIMES, SQUID_ITEM_ROTATION_X));
  }

  public static void applyToArm(ModelPart arm, HumanoidArm side, ArmPose pose) {
    float mirror = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
    float xRot = -pose.rotationX() * Mth.DEG_TO_RAD;
    float yRot = mirror * pose.rotationY() * Mth.DEG_TO_RAD;
    float zRot = mirror * pose.rotationZ() * Mth.DEG_TO_RAD;

    // The BB arm pivot is one pixel inward from vanilla's arm pivot.
    float pivotDifference = -mirror;
    float cosY = Mth.cos(yRot);
    float sinY = Mth.sin(yRot);
    float cosZ = Mth.cos(zRot);
    float sinZ = Mth.sin(zRot);
    float correctionX = pivotDifference * cosZ * cosY;
    float correctionY = pivotDifference * sinZ * cosY;
    float correctionZ = pivotDifference * -sinY;

    arm.x = -mirror * (4.0F + pose.positionX()) + correctionX;
    arm.y = 2.0F - pose.positionY() + correctionY;
    arm.z = pose.positionZ() + correctionZ;
    arm.xRot = xRot;
    arm.yRot = yRot;
    arm.zRot = zRot;
  }

  public static void transformItem(PoseStack poseStack, HumanoidArm side, ArmPose pose) {
    float mirror = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
    poseStack.translate(
        -mirror / 16.0F, (9.0F - pose.itemPositionY()) / 16.0F, -6.0F / 16.0F);
    poseStack.mulPose(Axis.XP.rotationDegrees(180.0F + pose.itemRotationX()));
    poseStack.translate(0.0F, 7.0F / 16.0F, 2.0F / 16.0F);
  }

  private static float catmullRom(float time, float[] times, float[] values) {
    if (time <= times[0]) return values[0];
    int last = times.length - 1;
    if (time >= times[last]) return values[last];

    int right = 1;
    while (time > times[right]) right++;
    int left = right - 1;
    int before = Math.max(0, left - 1);
    int after = Math.min(last, right + 1);
    float progress = (time - times[left]) / (times[right] - times[left]);
    float progress2 = progress * progress;
    float progress3 = progress2 * progress;
    float p0 = values[before];
    float p1 = values[left];
    float p2 = values[right];
    float p3 = values[after];
    return 0.5F
        * ((2.0F * p1)
            + (-p0 + p2) * progress
            + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * progress2
            + (-p0 + 3.0F * p1 - 3.0F * p2 + p3) * progress3);
  }

  public record ArmPose(
      float positionX,
      float positionY,
      float positionZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      float itemPositionY,
      float itemRotationX) {}
}
