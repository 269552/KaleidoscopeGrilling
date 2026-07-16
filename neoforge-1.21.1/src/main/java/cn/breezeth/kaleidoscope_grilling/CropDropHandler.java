package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class CropDropHandler {
    public static void onBlockBreak(BlockEvent.BreakEvent event){
        if(event.getLevel().isClientSide()||!event.getState().is(Blocks.SHORT_GRASS))return;
        ItemStack hat=event.getPlayer().getItemBySlot(EquipmentSlot.HEAD);
        String id=BuiltInRegistries.ITEM.getKey(hat.getItem()).toString();
        if(!id.equals("kaleidoscope_cookery:straw_hat")&&!id.equals("kaleidoscope_cookery:straw_hat_flower"))return;
        int fortune=event.getPlayer().getMainHandItem().getEnchantmentLevel(event.getPlayer().level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE));
        tryDrop(event,new ItemStack(ModItems.CANOLA_SEEDS.get(),1+event.getPlayer().getRandom().nextInt(1+fortune)));
        tryDrop(event,new ItemStack(ModItems.SWEET_POTATO.get(),1+event.getPlayer().getRandom().nextInt(1+fortune)));
        tryDrop(event,new ItemStack(ModItems.ONION.get(),1+event.getPlayer().getRandom().nextInt(1+fortune)));
    }
    private static void tryDrop(BlockEvent.BreakEvent event,ItemStack stack){if(event.getPlayer().getRandom().nextFloat()<0.125F)Block.popResource((net.minecraft.world.level.Level)event.getLevel(),event.getPos(),stack);}
    private CropDropHandler(){}
}
