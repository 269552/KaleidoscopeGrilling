package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class CropDropHandler {
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide() || !event.getState().is(Blocks.GRASS)) return;
        ItemStack hat=event.getPlayer().getItemBySlot(EquipmentSlot.HEAD);
        var id=ForgeRegistries.ITEMS.getKey(hat.getItem());
        if(id==null||(!id.toString().equals("kaleidoscope_cookery:straw_hat")&&!id.toString().equals("kaleidoscope_cookery:straw_hat_flower")))return;
        int fortune=EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE,event.getPlayer().getMainHandItem());
        tryDrop(event, new ItemStack(ModItems.CANOLA_SEEDS.get(),1+event.getPlayer().getRandom().nextInt(1+fortune)));
        tryDrop(event, new ItemStack(ModItems.SWEET_POTATO.get(),1+event.getPlayer().getRandom().nextInt(1+fortune)));
        tryDrop(event, new ItemStack(ModItems.ONION.get(),1+event.getPlayer().getRandom().nextInt(1+fortune)));
    }
    private static void tryDrop(BlockEvent.BreakEvent event,ItemStack stack){if(event.getPlayer().getRandom().nextFloat()<0.125F)Block.popResource((net.minecraft.world.level.Level)event.getLevel(),event.getPos(),stack);}
    private CropDropHandler(){}
}
