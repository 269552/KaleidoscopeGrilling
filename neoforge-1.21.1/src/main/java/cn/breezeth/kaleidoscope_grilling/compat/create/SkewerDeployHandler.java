package cn.breezeth.kaleidoscope_grilling.compat.create;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.ModItems;
import cn.breezeth.kaleidoscope_grilling.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.SkewerCompatApi;
import cn.breezeth.kaleidoscope_grilling.SkewerRecipes;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/** Lets a Create Deployer start working on skewerable targets; actual skewering happens in the mixin. */
public final class SkewerDeployHandler {
  public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
    if (event.getBlockEntity().getLevel() == null
        || event.getBlockEntity().getLevel().isClientSide) return;
    RecipeWrapper inv = event.getInventory();
    if (inv.size() < 2) return;
    ItemStack target = inv.getItem(0);
    ItemStack held =
        event.getBlockEntity().getPlayer() == null
            ? ItemStack.EMPTY
            : event.getBlockEntity().getPlayer().getMainHandItem();
    if (!isSkewerTarget(target) || held.isEmpty()) return;
    if (!SkewerRecipes.isConfiguredIngredient(held) && !SkewerCompatApi.canSkewer(held, null))
      return;
    event.addRecipe(
        () ->
            Optional.of(
                new RecipeHolder<>(
                    ResourceLocation.fromNamespaceAndPath(
                        KaleidoscopeGrilling.MOD_ID, "skewer_deploy"),
                    new SkewerDeployRecipe())),
        200);
  }

  private static boolean isSkewerTarget(ItemStack stack) {
    return stack.is(Items.STICK)
        || stack.is(ModItems.UNFINISHED_SKEWER.get())
        || (stack.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(stack));
  }

  private SkewerDeployHandler() {}
}
