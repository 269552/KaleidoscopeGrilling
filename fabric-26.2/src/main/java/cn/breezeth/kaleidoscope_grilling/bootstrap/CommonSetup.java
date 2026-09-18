package cn.breezeth.kaleidoscope_grilling.bootstrap;

import cn.breezeth.kaleidoscope_grilling.compat.CreateCompat;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import net.minecraft.world.level.block.ComposterBlock;

/** Fabric replacement for the old FML common setup event. */
public final class CommonSetup {
    public static void onSetup() {
        CreateCompat.register();
        ComposterBlock.COMPOSTABLES.put(ModItems.CANOLA_SEEDS.get(), 0.30F);
        ComposterBlock.COMPOSTABLES.put(ModItems.ONION.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.SWEET_POTATO.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModItems.HOUTTUYNIA.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.PEPPER_LEAVES_ITEM.get(), 0.30F);
        ComposterBlock.COMPOSTABLES.put(ModBlocks.PEPPER_SAPLING_ITEM.get(), 0.30F);
    }

    private CommonSetup() {}
}
