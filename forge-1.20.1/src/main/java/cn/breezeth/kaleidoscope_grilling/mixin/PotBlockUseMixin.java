package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.*;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotBlock.class)
public abstract class PotBlockUseMixin {
  @Inject(method = "use", at = @At("HEAD"), cancellable = true)
  private void grilling$season(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<InteractionResult> cir) {
    if (hand != InteractionHand.MAIN_HAND) return;
    ItemStack held = player.getItemInHand(hand);
    if (!held.is(ModItems.SPECIAL_SEASONING.get())) return;
    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof SeasonedPotAccess access)) return;
    if (!HotFoodConfig.ENABLE_COOKERY_HEAT_AND_SEASONING.get()) {
      if (!level.isClientSide)
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "message.kaleidoscope_grilling.cookery_integration_disabled"),
            true);
      cir.setReturnValue(InteractionResult.SUCCESS);
      return;
    }
    if (!level.isClientSide) SeasoningUse.apply(player, hand, held, access);
    cir.setReturnValue(InteractionResult.SUCCESS);
  }
}
