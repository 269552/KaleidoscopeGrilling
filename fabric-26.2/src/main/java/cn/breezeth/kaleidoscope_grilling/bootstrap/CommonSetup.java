package cn.breezeth.kaleidoscope_grilling.bootstrap;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import net.minecraft.world.level.block.ComposterBlock;

public final class CommonSetup {
  public static void init() {
    ComposterBlock.COMPOSTABLES.put(ModItems.CANOLA_SEEDS.get(), 0.30F);
    ComposterBlock.COMPOSTABLES.put(ModItems.ONION.get(), 0.65F);
    ComposterBlock.COMPOSTABLES.put(ModItems.SWEET_POTATO.get(), 0.65F);
    ComposterBlock.COMPOSTABLES.put(ModItems.HOUTTUYNIA.get(), 0.65F);
    ComposterBlock.COMPOSTABLES.put(ModBlocks.PEPPER_LEAVES_ITEM.get(), 0.30F);
    ComposterBlock.COMPOSTABLES.put(ModBlocks.PEPPER_SAPLING_ITEM.get(), 0.30F);
  }

  private CommonSetup() {}
}
