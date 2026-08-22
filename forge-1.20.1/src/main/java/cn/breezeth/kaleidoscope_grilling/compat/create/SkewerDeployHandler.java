package cn.breezeth.kaleidoscope_grilling.compat.create;

import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.skewer.SecretSkewerItem;
import cn.breezeth.kaleidoscope_grilling.SkewerCompatApi;
import cn.breezeth.kaleidoscope_grilling.SeasoningAutomationApi;
import cn.breezeth.kaleidoscope_grilling.skewer.SkewerRecipes;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/** Lets a Create Deployer start working on skewerable targets; actual skewering happens in the mixin. */
public final class SkewerDeployHandler {
  public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
    if (event.getBlockEntity().getLevel() == null
        || event.getBlockEntity().getLevel().isClientSide) return;
    RecipeWrapper inv = event.getInventory();
    if (inv.getContainerSize() < 2) return;
    ItemStack target = inv.getItem(0);
    ItemStack held =
        event.getBlockEntity().getPlayer() == null
            ? ItemStack.EMPTY
            : event.getBlockEntity().getPlayer().getMainHandItem();
    if (held.isEmpty()) return;
    if (!SeasoningAutomationApi.appendIngredient(target, held).isEmpty()) {
      event.addRecipe(() -> Optional.of(new SkewerDeployRecipe()), 250);
      return;
    }
    if (!isSkewerTarget(target)) return;
    if (!SkewerRecipes.isConfiguredIngredient(held) && !SkewerCompatApi.canSkewer(held, null))
      return;
    event.addRecipe(() -> Optional.of(new SkewerDeployRecipe()), 200);
  }

  private static boolean isSkewerTarget(ItemStack stack) {
    return stack.is(Items.STICK)
        || stack.is(ModItems.UNFINISHED_SKEWER.get())
        || (stack.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(stack));
  }

  private SkewerDeployHandler() {}
}
