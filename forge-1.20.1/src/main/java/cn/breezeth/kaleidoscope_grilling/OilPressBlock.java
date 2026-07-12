package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class OilPressBlock extends BaseEntityBlock {
    public OilPressBlock(Properties properties){super(properties);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new OilPressBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,ModBlockEntities.OIL_PRESS.get(),OilPressBlockEntity::serverTick);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(level.getBlockEntity(pos)instanceof OilPressBlockEntity press))return InteractionResult.PASS;
        ItemStack held=player.getItemInHand(hand);
        if(held.is(ModItems.OIL_CAKE.get())){if(!level.isClientSide){if(press.addCake()){if(!player.getAbilities().instabuild)held.shrink(1);}else player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.press_full"),true);}return InteractionResult.sidedSuccess(level.isClientSide);}
        if(held.isEmpty()&&press.residue()>0){if(!level.isClientSide)player.addItem(new ItemStack(ModItems.OIL_RESIDUE.get(),press.takeResidue()));return InteractionResult.sidedSuccess(level.isClientSide);}
        if(!level.isClientSide)player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.press_status",press.cakes(),press.progress(),OilPressBlockEntity.REQUIRED_PROGRESS),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
