package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class DragonEggPowderHandler {
    private static final TagKey<Item> KNIVES=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery","kitchen_knife"));
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        ItemStack knife=event.getItemStack();
        Level level=event.getLevel();
        BlockPos pos=event.getPos();
        if(level.isClientSide||!knife.is(KNIVES)||!level.getBlockState(pos).is(Blocks.DRAGON_EGG))return;
        var holder=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
        int looting=EnchantmentHelper.getItemEnchantmentLevel(holder,knife);
        int count=1+(looting==0?0:level.random.nextInt(looting+1));
        event.getEntity().addItem(new ItemStack(ModItems.DRAGON_EGG_POWDER.get(),count));
        knife.hurtAndBreak(1,event.getEntity(),net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }

    private DragonEggPowderHandler(){}
}
