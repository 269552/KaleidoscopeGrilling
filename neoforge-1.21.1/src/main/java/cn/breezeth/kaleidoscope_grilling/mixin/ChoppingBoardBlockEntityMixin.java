package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets =
        "com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ChoppingBoardBlockEntity")
public abstract class ChoppingBoardBlockEntityMixin {
  @Shadow
  public abstract int getMaxCutCount();

  @Shadow
  public abstract int getCurrentCutCount();

  @Shadow
  public abstract ItemStack getCurrentCutStack();

  @Unique private boolean grilling$dropChickenSkin;

  @ModifyArg(
      method = "playParticlesSound",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/level/ServerLevel;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"),
      index = 2)
  private SoundEvent grilling$useWaterSoundForSweetPotatoPowder(SoundEvent original) {
    if (getCurrentCutCount() == 2
        && getCurrentCutStack().is(ModItems.SWEET_POTATO_POWDER.get())) {
      return SoundEvents.BUCKET_EMPTY;
    }
    return original;
  }

  @Inject(method = "onCutItem", at = @At("HEAD"))
  private void grilling$captureChickenCompletion(
      Level level,
      LivingEntity user,
      ItemStack knife,
      CallbackInfoReturnable<Boolean> cir) {
    grilling$dropChickenSkin =
        getCurrentCutStack().is(Items.CHICKEN)
            && getCurrentCutCount() >= getMaxCutCount()
            && getMaxCutCount() > 0;
  }

  @Inject(method = "onCutItem", at = @At("RETURN"))
  private void grilling$dropChickenSkin(
      Level level,
      LivingEntity user,
      ItemStack knife,
      CallbackInfoReturnable<Boolean> cir) {
    if (grilling$dropChickenSkin && cir.getReturnValueZ() && !level.isClientSide) {
      BlockPos pos = ((BlockEntity) (Object) this).getBlockPos();
      net.minecraft.world.level.block.Block.popResource(
          level, pos, new ItemStack(ModItems.CHICKEN_SKIN.get(), 1 + level.random.nextInt(3)));
    }
    grilling$dropChickenSkin = false;
  }
}
