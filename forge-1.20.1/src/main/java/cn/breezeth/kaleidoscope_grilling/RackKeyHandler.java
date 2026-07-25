package cn.breezeth.kaleidoscope_grilling;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class RackKeyHandler {
  public static final KeyMapping KEY =
      new KeyMapping(
          "key.kaleidoscope_grilling.rack_swap",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_CAPS_LOCK,
          "key.categories.kaleidoscope_grilling");
  private static boolean held;
  private static boolean capsHeld;

  @Mod.EventBusSubscriber(
      modid = KaleidoscopeGrilling.MOD_ID,
      bus = Mod.EventBusSubscriber.Bus.MOD,
      value = Dist.CLIENT)
  public static final class Registration {
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
      event.register(KEY);
    }
  }

  @Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
  public static final class Input {
    @SubscribeEvent
    public static void key(InputEvent.Key event) {
      if (!KEY.matches(event.getKey(), event.getScanCode())) return;
      if (event.getAction() == GLFW.GLFW_PRESS) {
        capsHeld = true;
        beginHold();
      } else if (event.getAction() == GLFW.GLFW_RELEASE) {
        capsHeld = false;
        finishHold();
      }
    }
  }

  public static boolean isHeld() {
    return held;
  }

  public static boolean isCapsHeld() {
    return capsHeld;
  }

  public static void finishHold() {
    held = false;
  }

  private static void beginHold() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null
        || minecraft.player == null
        || minecraft.player.isSpectator()
        || minecraft.getConnection() == null) return;
    held = true;
    minecraft.getConnection().sendCommand("kgrack");
  }

  private RackKeyHandler() {}
}
