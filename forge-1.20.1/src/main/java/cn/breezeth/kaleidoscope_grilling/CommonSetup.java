package cn.breezeth.kaleidoscope_grilling;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
public final class CommonSetup{public static void onSetup(FMLCommonSetupEvent event){event.enqueueWork(()->{ComposterBlock.COMPOSTABLES.put(ModItems.OIL_RESIDUE.get(),0.65F);ComposterBlock.COMPOSTABLES.put(ModItems.CANOLA_SEEDS.get(),0.30F);ComposterBlock.COMPOSTABLES.put(ModItems.ONION.get(),0.65F);ComposterBlock.COMPOSTABLES.put(ModItems.SWEET_POTATO.get(),0.65F);ComposterBlock.COMPOSTABLES.put(ModItems.HOUTTUYNIA.get(),0.65F);ComposterBlock.COMPOSTABLES.put(ModBlocks.PEPPER_LEAVES_ITEM.get(),0.30F);ComposterBlock.COMPOSTABLES.put(ModBlocks.PEPPER_SAPLING_ITEM.get(),0.30F);});}private CommonSetup(){}}
