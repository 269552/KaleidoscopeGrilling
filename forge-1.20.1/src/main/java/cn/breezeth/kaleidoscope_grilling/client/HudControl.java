package cn.breezeth.kaleidoscope_grilling.client;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class HudControl {
  private static boolean enabled;

  public static boolean isEnabled() {
    return enabled;
  }

  @SubscribeEvent
  public static void registerCommands(RegisterClientCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("kg")
                .then(
                    Commands.literal("hud")
                        .then(Commands.literal("on").executes(context -> set(true)))
                        .then(Commands.literal("off").executes(context -> set(false))))
                .then(
                    Commands.literal("dev")
                        .then(debugAnimation("animation1", MultiBiteSkewerItem.AnimationProfile.ONE))
                        .then(debugAnimation("animation2", MultiBiteSkewerItem.AnimationProfile.TWO))
                        .then(debugAnimation("animation3", MultiBiteSkewerItem.AnimationProfile.THREE))
                        .then(Commands.literal("off").executes(context -> disableAnimationDebug()))));
  }

  private static com.mojang.brigadier.builder.ArgumentBuilder<
          net.minecraft.commands.CommandSourceStack, ?>
      debugAnimation(String name, MultiBiteSkewerItem.AnimationProfile profile) {
    return Commands.literal(name)
        .then(
            Commands.argument("seconds", FloatArgumentType.floatArg(0.0F, 5.0F))
                .executes(
                    context ->
                        setAnimationDebug(
                            profile, FloatArgumentType.getFloat(context, "seconds"))));
  }

  private static int setAnimationDebug(
      MultiBiteSkewerItem.AnimationProfile profile, float seconds) {
    SkewerAnimationDebug.enable(profile, seconds);
    sendDebugMessage();
    return 1;
  }

  private static int disableAnimationDebug() {
    SkewerAnimationDebug.disable();
    sendDebugMessage();
    return 1;
  }

  private static void sendDebugMessage() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null) {
      minecraft.player.sendSystemMessage(
          Component.literal(
              SkewerAnimationDebug.isEnabled()
                  ? String.format(
                      "Animation debug: %s @ %.5fs",
                      SkewerAnimationDebug.animationName(), SkewerAnimationDebug.seconds())
                  : "Animation debug: off"));
    }
  }

  private static int set(boolean value) {
    enabled = value;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null) {
      minecraft.player.sendSystemMessage(
          Component.translatable("command.kaleidoscope_grilling.hud." + (enabled ? "on" : "off")));
    }
    return 1;
  }

  private HudControl() {}
}
