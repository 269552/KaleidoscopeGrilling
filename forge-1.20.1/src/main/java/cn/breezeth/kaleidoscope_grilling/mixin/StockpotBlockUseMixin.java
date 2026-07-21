package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.*;
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

@Mixin(
    targets = "com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock",
    remap = false)
public abstract class StockpotBlockUseMixin {
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
    if (!level.isClientSide) SeasoningUse.apply(player, hand, held, access);
    cir.setReturnValue(InteractionResult.SUCCESS);
  }
}
