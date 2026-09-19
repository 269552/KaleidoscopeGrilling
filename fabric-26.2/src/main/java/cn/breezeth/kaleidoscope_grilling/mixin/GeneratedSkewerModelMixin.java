package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Substitutes Grilling's generated skewer geometry for configured external items. */
@Mixin(ItemRenderer.class)
public abstract class GeneratedSkewerModelMixin {
  private static final ModelResourceLocation GENERATED_MODEL =
      ModelResourceLocation.inventory(
          ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "secret_skewer"));

  @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
  private void grilling$generatedSkewerModel(
      ItemStack stack,
      @Nullable Level level,
      @Nullable LivingEntity entity,
      int seed,
      CallbackInfoReturnable<BakedModel> cir) {
    if (!SkewerRecipes.usesGeneratedModel(stack)) return;
    BakedModel base =
        Minecraft.getInstance().getModelManager().getModel(GENERATED_MODEL);
    ItemOverrides overrides = base.getOverrides();
    ClientLevel clientLevel = level instanceof ClientLevel client ? client : null;
    BakedModel resolved = overrides.resolve(base, stack, clientLevel, entity, seed);
    cir.setReturnValue(resolved == null ? base : resolved);
  }
}
