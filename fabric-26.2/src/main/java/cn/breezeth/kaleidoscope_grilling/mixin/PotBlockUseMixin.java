package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;

import cn.breezeth.kaleidoscope_grilling.food.HotFoodConfig;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasonedPotAccess;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningUse;

import cn.breezeth.kaleidoscope_grilling.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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

@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock")
public abstract class PotBlockUseMixin {
  @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
  private void grilling$season(
      ItemStack held,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<ItemInteractionResult> cir) {
    if (hand != InteractionHand.MAIN_HAND || !held.is(ModItems.SPECIAL_SEASONING.get())) return;
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (!(blockEntity instanceof SeasonedPotAccess seasoned)) return;
    if (!HotFoodConfig.ENABLE_COOKERY_HEAT_AND_SEASONING.get()) {
      if (!level.isClientSide)
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "message.kaleidoscope_grilling.cookery_integration_disabled"),
            true);
      cir.setReturnValue(ItemInteractionResult.SUCCESS);
      return;
    }
    if (!level.isClientSide) SeasoningUse.apply(player, hand, held, seasoned);
    cir.setReturnValue(ItemInteractionResult.SUCCESS);
  }
}
