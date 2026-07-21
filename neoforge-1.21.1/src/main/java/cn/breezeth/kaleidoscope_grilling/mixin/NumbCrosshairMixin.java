package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.ModEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class NumbCrosshairMixin {
  @Unique private boolean grilling$numbCrosshairPose;

  @Inject(
      method =
          "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
      at = @At("HEAD"))
  private void grilling$moveNumbCrosshair(
      GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
    Minecraft minecraft = Minecraft.getInstance();
    grilling$numbCrosshairPose =
        minecraft.player != null
            && minecraft.options.getCameraType().isFirstPerson()
            && minecraft.player.hasEffect(ModEffects.NUMB);
    if (!grilling$numbCrosshairPose) return;
    double angle = System.nanoTime() * 0.0000000022D;
    graphics.pose().pushPose();
    graphics.pose().translate(Math.cos(angle) * 24.0D, Math.sin(angle) * 24.0D, 0.0D);
  }

  @Inject(
      method =
          "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
      at = @At("RETURN"))
  private void grilling$restoreNumbCrosshair(
      GuiGraphics graphics, DeltaTracker tracker, CallbackInfo ci) {
    if (grilling$numbCrosshairPose) graphics.pose().popPose();
    grilling$numbCrosshairPose = false;
  }
}
