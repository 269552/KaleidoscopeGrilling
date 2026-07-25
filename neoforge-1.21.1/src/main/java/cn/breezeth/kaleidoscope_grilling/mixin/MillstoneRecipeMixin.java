package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.output.RandomOutput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MillstoneRecipe.class, remap = false)
public abstract class MillstoneRecipeMixin {
  @Shadow @Final @Mutable private Ingredient ingredient;

  @Shadow @Final private List<RandomOutput> results;

  @Inject(method = "<init>", at = @At("RETURN"))
  private void kaleidoscopeGrilling$excludeCanolaFromCookeryOil(
      Ingredient ingredient, List<RandomOutput> results, CallbackInfo ci) {
    if (this.results.isEmpty()
        || !BuiltInRegistries.ITEM.getKey(this.results.getFirst().stack().getItem())
            .equals(ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "oil"))) {
      return;
    }

    this.ingredient =
        Ingredient.of(
            Arrays.stream(this.ingredient.getItems())
                .filter(stack -> !stack.is(ModItems.CANOLA_SEEDS.get())));
  }
}
