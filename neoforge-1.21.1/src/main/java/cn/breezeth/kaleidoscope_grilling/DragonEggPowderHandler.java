package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
        teleportEgg(level,pos);
    }

    private static void teleportEgg(Level level,BlockPos pos){
        BlockState state=level.getBlockState(pos);
        RandomSource random=level.random;
        for(int i=0;i<1000;i++){
            BlockPos target=pos.offset(random.nextInt(16)-8,random.nextInt(8)-4,random.nextInt(16)-8);
            if(level.getBlockState(target).isAir()){
                level.setBlock(target,state,2);
                level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
                if(level instanceof ServerLevel server){
                    double dx=target.getX()-pos.getX(),dy=target.getY()-pos.getY(),dz=target.getZ()-pos.getZ();
                    server.sendParticles(ParticleTypes.PORTAL,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,64,dx*0.5,dy*0.5,dz*0.5,0.15);
                    server.sendParticles(ParticleTypes.PORTAL,target.getX()+0.5,target.getY()+0.5,target.getZ()+0.5,64,-dx*0.5,-dy*0.5,-dz*0.5,0.15);
                }
                level.playSound(null,pos,SoundEvents.ENDERMAN_TELEPORT,SoundSource.BLOCKS,1.0F,1.0F);
                level.playSound(null,target,SoundEvents.ENDERMAN_TELEPORT,SoundSource.BLOCKS,1.0F,1.0F);
                return;
            }
        }
    }

    private DragonEggPowderHandler(){}
}
