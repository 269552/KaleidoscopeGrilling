package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.compat.create.CreateSeasoningCompat;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import java.util.List;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MechanicalMixerBlockEntity.class)
public abstract class MechanicalMixerSeasoningMixin {
  @Unique private boolean grilling$seasoningDurationSet;

  @Inject(method = "getMatchingRecipes", at = @At("RETURN"), remap = false)
  private void grilling$addSeasoningRecipe(CallbackInfoReturnable<List<Recipe<?>>> cir) {
    MechanicalMixerBlockEntity mixer = (MechanicalMixerBlockEntity) (Object) this;
    if (mixer.getLevel() == null || mixer.getLevel().isClientSide) return;
    BlockEntity blockEntity = mixer.getLevel().getBlockEntity(mixer.getBlockPos().below(2));
    if (!(blockEntity instanceof BasinBlockEntity basin)
        || !CreateSeasoningCompat.process(basin, true)) return;
    List<Recipe<?>> recipes = cir.getReturnValue();
    if (!recipes.contains(CreateSeasoningCompat.recipe())) recipes.add(0, CreateSeasoningCompat.recipe());
  }

  @Inject(method = "tick", at = @At("TAIL"), remap = false)
  private void grilling$fixSeasoningDuration(CallbackInfo ci) {
    MechanicalMixerBlockEntity mixer = (MechanicalMixerBlockEntity) (Object) this;
    Recipe<?> currentRecipe =
        ((BasinOperatingBlockEntityAccessor) mixer).grilling$getCurrentRecipe();
    if (!mixer.running || !CreateSeasoningCompat.isRecipe(currentRecipe)) {
      grilling$seasoningDurationSet = false;
      return;
    }
    if (!grilling$seasoningDurationSet
        && mixer.getLevel() != null
        && !mixer.getLevel().isClientSide
        && mixer.runningTicks == 20
        && mixer.processingTicks > 0) {
      mixer.processingTicks = 80;
      grilling$seasoningDurationSet = true;
    }
  }
}
