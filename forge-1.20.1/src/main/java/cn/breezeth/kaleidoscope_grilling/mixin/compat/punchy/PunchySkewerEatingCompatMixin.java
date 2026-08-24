package cn.breezeth.kaleidoscope_grilling.mixin.compat.punchy;

import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets Grilling own first-person rendering only while a skewer eating animation is active. */
@Pseudo
@Mixin(targets = "punchy.config.PunchyConfig", remap = false)
public abstract class PunchySkewerEatingCompatMixin {
  @Inject(method = "isHandBlacklisted", at = @At("HEAD"), cancellable = true, remap = false)
  private static void grilling$yieldActiveSkewerHand(
      Player player, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
    // Grilling owns both hands here: some profiles animate the off hand, while
    // the remaining profiles intentionally suppress it.
    if (player != null
        && player.isUsingItem()
        && SkewerRecipes.usesCustomEating(player.getUseItem())) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "isItemBlacklisted", at = @At("HEAD"), cancellable = true, remap = false)
  private static void grilling$yieldActiveSkewerItem(
      ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    LocalPlayer player = Minecraft.getInstance().player;
    if (player != null
        && player.isUsingItem()
        && SkewerRecipes.usesCustomEating(player.getUseItem())
        && ItemStack.isSameItemSameTags(stack, player.getUseItem())) {
      cir.setReturnValue(true);
    }
  }
}
