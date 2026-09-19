package cn.breezeth.kaleidoscope_grilling.client;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/** Temporary client-only keyframe override used to compare Blockbench and in-game poses. */
public final class SkewerAnimationDebug {
  private static MultiBiteSkewerItem.AnimationProfile profile;
  private static float seconds;

  public static void enable(MultiBiteSkewerItem.AnimationProfile value, float time) {
    profile = value;
    seconds = Math.max(0.0F, Math.min(length(value), time));
  }

  public static void disable() {
    profile = null;
    seconds = 0.0F;
  }

  public static boolean isEnabled() {
    return profile != null;
  }

  public static MultiBiteSkewerItem.AnimationProfile profile(
      int entityId, MultiBiteSkewerItem.AnimationProfile fallback) {
    Minecraft minecraft = Minecraft.getInstance();
    return profile != null && minecraft.player != null && minecraft.player.getId() == entityId
        ? profile
        : fallback;
  }

  public static float seconds(Player player, float fallback) {
    Minecraft minecraft = Minecraft.getInstance();
    return profile != null && minecraft.player == player ? seconds : fallback;
  }

  public static float seconds() {
    return seconds;
  }

  public static String animationName() {
    if (profile == MultiBiteSkewerItem.AnimationProfile.ONE) return "animation1";
    if (profile == MultiBiteSkewerItem.AnimationProfile.TWO) return "animation2";
    if (profile == MultiBiteSkewerItem.AnimationProfile.THREE) return "animation3";
    return "off";
  }

  private static float length(MultiBiteSkewerItem.AnimationProfile value) {
    return value == MultiBiteSkewerItem.AnimationProfile.THREE ? 5.0F : 4.5F;
  }

  private SkewerAnimationDebug() {}
}
