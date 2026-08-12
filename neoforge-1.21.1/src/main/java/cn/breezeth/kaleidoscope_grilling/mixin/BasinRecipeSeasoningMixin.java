package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.compat.create.CreateSeasoningCompat;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BasinRecipe.class)
public abstract class BasinRecipeSeasoningMixin {
  @Inject(method = "match", at = @At("HEAD"), cancellable = true, remap = false)
  private static void grilling$matchSeasoning(
      BasinBlockEntity basin, Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
    if (CreateSeasoningCompat.isRecipe(recipe))
      cir.setReturnValue(CreateSeasoningCompat.process(basin, true));
  }

  @Inject(method = "apply", at = @At("HEAD"), cancellable = true, remap = false)
  private static void grilling$applySeasoning(
      BasinBlockEntity basin, Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
    if (CreateSeasoningCompat.isRecipe(recipe))
      cir.setReturnValue(CreateSeasoningCompat.process(basin, false));
  }
}
