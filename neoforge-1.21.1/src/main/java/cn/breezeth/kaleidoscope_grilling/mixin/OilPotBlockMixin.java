package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.OilPotVisualState;
import cn.breezeth.kaleidoscope_grilling.TypedOilPotAccess;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.OilPotBlock")
public abstract class OilPotBlockMixin {
  @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
  private void grilling$blockWrongOilExtraction(
      ItemStack held,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit,
      CallbackInfoReturnable<ItemInteractionResult> cir) {
    if (hand == InteractionHand.MAIN_HAND
        && player.getMainHandItem().isEmpty()
        && level.getBlockEntity(pos) instanceof TypedOilPotAccess access
        && !access.grilling$getOilType().isEmpty()) {
      cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }
  }

  @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
  private void grilling$addOilType(
      StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
    builder.add(OilPotVisualState.OIL_TYPE);
  }

  @Inject(method = "setPlacedBy", at = @At("TAIL"))
  private void grilling$placed(
      Level level,
      BlockPos pos,
      BlockState state,
      @Nullable LivingEntity placer,
      ItemStack stack,
      CallbackInfo ci) {
    if (level.getBlockEntity(pos) instanceof TypedOilPotAccess access) {
      access.grilling$setOilType(OilPotCompat.getType(stack));
    }
  }

  @Inject(method = "getDrops", at = @At("RETURN"))
  private void grilling$drops(
      BlockState state, LootParams.Builder params, CallbackInfoReturnable<List<ItemStack>> cir) {
    BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (be instanceof TypedOilPotAccess access) {
      cir.getReturnValue().stream()
          .filter(OilPotCompat::isOilPot)
          .forEach(stack -> OilPotCompat.setType(stack, access.grilling$getOilType()));
    }
  }
}
