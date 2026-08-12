package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.OilPressBlock;
import cn.breezeth.kaleidoscope_grilling.OilPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode;
import com.simibubi.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lets a Create Mechanical Press press an oil press below, granting 8 progress per stroke. */
@Mixin(PressingBehaviour.class)
public abstract class PressingBehaviourMixin {
  @Shadow(remap = false) public int runningTicks;
  @Shadow(remap = false) public boolean running;
  @Shadow(remap = false) public PressingBehaviourSpecifics specifics;

  @Shadow(remap = false) public abstract void start(Mode mode);

  @Unique private boolean grilling$pressingOilPress;

  @Inject(method = "tick", at = @At("HEAD"), remap = false)
  private void grilling$scanOilPressBelow(CallbackInfo ci) {
    if (running || specifics.getKineticSpeed() == 0) return;
    BlockEntityBehaviour behaviour = (BlockEntityBehaviour) (Object) this;
    Level level = behaviour.getWorld();
    if (level == null || level.isClientSide) return;
    // The oil press sits at the mechanical press working face, with one air block between them.
    BlockPos pressPos = behaviour.getPos().below(2);
    if (!(level.getBlockState(pressPos).getBlock() instanceof OilPressBlock)) return;
    if (!(level.getBlockEntity(pressPos) instanceof OilPressBlockEntity press)) return;
    if (press.waitingForContainer()
        || press.progress() >= OilPressBlockEntity.REQUIRED_PROGRESS
        || press.cakes() < OilPressBlockEntity.MAX_CAKES) return;
    grilling$pressingOilPress = true;
    start(Mode.WORLD);
  }

  @Inject(
      method = "tick",
      at =
          @At(
              value = "INVOKE",
              target = "Lcom/simibubi/create/content/kinetics/press/PressingBehaviour;applyInWorld()V",
              shift = At.Shift.BEFORE),
      remap = false)
  private void grilling$applyOilPressProgress(CallbackInfo ci) {
    if (!grilling$pressingOilPress) return;
    grilling$pressingOilPress = false;
    BlockEntityBehaviour behaviour = (BlockEntityBehaviour) (Object) this;
    Level level = behaviour.getWorld();
    if (level == null || level.isClientSide) return;
    BlockPos below = behaviour.getPos().below(2);
    if (level.getBlockEntity(below) instanceof OilPressBlockEntity press
        && press.cakes() == OilPressBlockEntity.MAX_CAKES) press.addProgress(8);
  }
}
