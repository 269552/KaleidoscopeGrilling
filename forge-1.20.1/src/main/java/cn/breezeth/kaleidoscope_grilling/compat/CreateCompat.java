package cn.breezeth.kaleidoscope_grilling.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

/** Keeps optional Create classes outside the normal Grilling class-loading path. */
public final class CreateCompat {
  public static boolean loaded() {
    return ModList.get().isLoaded("create");
  }

  public static void register() {
    if (!loaded()) return;
    cn.breezeth.kaleidoscope_grilling.compat.create.CreateOilPressCompat.registerHandlers();
    MinecraftForge.EVENT_BUS.addListener(
        cn.breezeth.kaleidoscope_grilling.compat.create.SkewerDeployHandler::onDeployerRecipeSearch);
  }

  public static ItemStack insertResidue(Level level, BlockPos pos, ItemStack residue) {
    if (!loaded()) return residue;
    return cn.breezeth.kaleidoscope_grilling.compat.create.CreateOilPressCompat
        .tryInsertResidueToBelt(level, pos, residue);
  }

  public static long[] fluidTankCapacity(Level level, BlockPos pos) {
    if (!loaded()) return null;
    return cn.breezeth.kaleidoscope_grilling.compat.create.CreateOilPressCompat
        .fluidTankCapacity(level, pos);
  }

  private CreateCompat() {}
}
