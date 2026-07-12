package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class BigVatBlock extends BaseEntityBlock {
    public static final MapCodec<BigVatBlock> CODEC=simpleCodec(BigVatBlock::new);
    public BigVatBlock(Properties properties){super(properties);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new BigVatBlockEntity(pos,state);}
    @Override protected ItemInteractionResult useItemOn(ItemStack held,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(level.getBlockEntity(pos)instanceof BigVatBlockEntity vat))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(held.is(Items.WATER_BUCKET))return insertBucket(level,player,hand,vat,"water");
        if(held.is(ModItems.CANOLA_OIL_BUCKET.get()))return insertBucket(level,player,hand,vat,"canola");
        if(held.is(Items.BUCKET)&&vat.buckets()>0)return extractBucket(level,player,hand,vat);
        if(OilPotCompat.isOilPot(held)&&vat.content().equals("canola"))return fillOilPot(level,player,held,vat);
        if(!level.isClientSide)player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.big_vat_status",vat.buckets(),BigVatBlockEntity.CAPACITY_BUCKETS),true);
        return ItemInteractionResult.SUCCESS;
    }
    private static ItemInteractionResult insertBucket(Level level,Player player,InteractionHand hand,BigVatBlockEntity vat,String type){if(!level.isClientSide){if(!vat.insert(type,1))player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.big_vat_reject"),true);else if(!player.getAbilities().instabuild)player.setItemInHand(hand,new ItemStack(Items.BUCKET));}return ItemInteractionResult.SUCCESS;}
    private static ItemInteractionResult extractBucket(Level level,Player player,InteractionHand hand,BigVatBlockEntity vat){if(!level.isClientSide){String type=vat.content();if(vat.extract(type,1))player.setItemInHand(hand,new ItemStack(type.equals("water")?Items.WATER_BUCKET:ModItems.CANOLA_OIL_BUCKET.get()));}return ItemInteractionResult.SUCCESS;}
    private static ItemInteractionResult fillOilPot(Level level,Player player,ItemStack pot,BigVatBlockEntity vat){if(!level.isClientSide){String current=OilPotCompat.getType(pot);int missing=64-OilPotCompat.getCount(pot);int bucketsNeeded=Math.min(vat.buckets(),missing/8);if((!current.isEmpty()&&!current.equals("canola"))||bucketsNeeded<=0)player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.big_vat_reject"),true);else if(vat.extract("canola",bucketsNeeded))OilPotCompat.fill(pot,"canola",bucketsNeeded*8);}return ItemInteractionResult.SUCCESS;}
}
