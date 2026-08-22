package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FillingRecipe.class, remap = false)
public abstract class FillingRecipeOilPotMixin {
  @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
  private void grilling$requireEmptyOilPot(
      RecipeWrapper input, Level level, CallbackInfoReturnable<Boolean> cir) {
    var stack = input.getItem(0);
    if (OilPotCompat.isOilPot(stack) && OilPotCompat.getCount(stack) > 0) {
      cir.setReturnValue(false);
    }
  }
}
