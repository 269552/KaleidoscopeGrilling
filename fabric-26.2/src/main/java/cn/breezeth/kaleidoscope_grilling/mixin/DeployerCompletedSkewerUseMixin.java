package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import cn.breezeth.kaleidoscope_grilling.skewer.SkeweringHandler;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents a Create Deployer from actively using or feeding completed skewers. */
@Mixin(DeployerBlockEntity.class)
public abstract class DeployerCompletedSkewerUseMixin {
  @Shadow protected DeployerFakePlayer player;

  @Inject(method = "activate", at = @At("HEAD"), cancellable = true, remap = false)
  private void grilling$blockCompletedSkewerUse(CallbackInfo ci) {
    if (player == null) return;
    ItemStack held = player.getMainHandItem();
    if (held.getItem() instanceof MultiBiteSkewerItem
        || SkewerRecipes.isRawSkewer(held)
        || SkewerRecipes.isCookedSkewer(held)
        || held.is(ModItems.SECRET_SKEWER.get()) && SkeweringHandler.ingredientCount(held) >= 3) {
      ci.cancel();
    }
  }
}
