package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.ChatFormatting;

public final class GrillBlock extends BaseEntityBlock {
    public static final MapCodec<GrillBlock> CODEC = simpleCodec(GrillBlock::new);
    public static final BooleanProperty LEGGED=BooleanProperty.create("legged");
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final TagKey<Item> EXTINGUISH_TOOLS=TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery","extinguish_stove"));
    private static final VoxelShape FLAT_Z=Block.box(0,0,2,16,4,14);
    private static final VoxelShape FLAT_X=Block.box(2,0,0,14,4,16);
    private static final VoxelShape LEGS_Z=Shapes.or(FLAT_Z,Block.box(0,0,2,2,4,4),Block.box(14,0,2,16,4,4),Block.box(0,0,12,2,4,14),Block.box(14,0,12,16,4,14));
    private static final VoxelShape LEGS_X=Shapes.or(FLAT_X,Block.box(2,0,0,4,4,2),Block.box(12,0,0,14,4,2),Block.box(2,0,14,4,4,16),Block.box(12,0,14,14,4,16));
    public GrillBlock(Properties properties) { super(properties);registerDefaultState(stateDefinition.any().setValue(LEGGED,false).setValue(LIT,false).setValue(FACING,Direction.NORTH)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(LEGGED,LIT,FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){BlockPos below=c.getClickedPos().below();return defaultBlockState().setValue(LEGGED,!c.getLevel().getBlockState(below).isFaceSturdy(c.getLevel(),below,Direction.UP)).setValue(LIT,false).setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos){if(direction==Direction.DOWN)return state.setValue(LEGGED,!neighborState.isFaceSturdy(level,neighborPos,Direction.UP));return super.updateShape(state,direction,neighborState,level,pos,neighborPos);}
    @Override protected VoxelShape getShape(BlockState s,net.minecraft.world.level.BlockGetter l,BlockPos p,CollisionContext c){boolean x=s.getValue(FACING).getAxis()==Direction.Axis.X;return s.getValue(LEGGED)?(x?LEGS_X:LEGS_Z):(x?FLAT_X:FLAT_Z);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GrillBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.GRILL.get(), GrillBlockEntity::tick);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.FLINT_AND_STEEL)) {
            if (!state.getValue(LIT)) {
                if (!level.isClientSide) {
                    level.setBlock(pos,state.setValue(LIT,true),Block.UPDATE_ALL);
                    level.playSound(null,pos,SoundEvents.FLINTANDSTEEL_USE,SoundSource.BLOCKS,1.0F,level.random.nextFloat()*0.4F+0.8F);
                    stack.hurtAndBreak(1,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
                }
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }
        if (stack.is(EXTINGUISH_TOOLS) && state.getValue(LIT)) {
            if (!level.isClientSide) {
                level.setBlock(pos,state.setValue(LIT,false),Block.UPDATE_ALL);
                level.playSound(null,pos,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,0.5F,
                        2.6F+(level.random.nextFloat()-level.random.nextFloat())*0.8F);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && OilPotCompat.isOilPot(stack)) {
            int needed = grill.getPhase() == 0 ? grill.getContainerSize() - (int) java.util.stream.IntStream.range(0, grill.getContainerSize()).filter(i -> grill.getItem(i).isEmpty()).count() : 0;
            if (needed > 0 && OilPotCompat.getCount(stack) >= needed) {
                if (!level.isClientSide) { OilPotCompat.consume(stack, grill.brushOil(OilPotCompat.heatDuration(stack)));level.playSound(null,pos,ModSounds.GRILL_FLIP.get(),SoundSource.BLOCKS,0.75F,1.0F); player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.oiled", needed), true); }
            } else if (!level.isClientSide) player.displayClientMessage(Component.translatable(needed == 0 ? "message.kaleidoscope_grilling.no_brushable_skewers" : "message.kaleidoscope_grilling.not_enough_oil"), true);
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && stack.is(ModItems.SPECIAL_SEASONING.get())) {
            int needed = grill.seasonableCount();
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            if (needed > 0 && remaining >= needed) {
                if (!level.isClientSide) { SeasoningAnimation.start(player); grill.season(stack);level.playSound(null,pos,ModSounds.SEASON.get(),SoundSource.BLOCKS,0.85F,1.0F); player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.grill_ready_to_take").withStyle(ChatFormatting.RED),true); stack.setDamageValue(stack.getDamageValue() + needed); if (stack.getDamageValue() >= stack.getMaxDamage()) player.setItemInHand(hand, new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get())); }
            } else if (!level.isClientSide) player.displayClientMessage(Component.translatable(needed == 0 ? "message.kaleidoscope_grilling.no_seasonable_skewers" : "message.kaleidoscope_grilling.not_enough_seasoning"), true);
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canAccept(stack)) {
            if (!state.getValue(LIT)) {
                if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.grill_need_heat"), true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!level.isClientSide) { grill.insert(stack, player); level.playSound(null,pos,ModSounds.ACTION_SUCCESS.get(),SoundSource.BLOCKS,0.65F,1.0F); }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canFlip()) {
            if (!level.isClientSide) {grill.flip();level.playSound(null,pos,ModSounds.GRILL_FLIP.get(),SoundSource.BLOCKS,0.75F,1.0F);player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.grill_wait_flip",grill.getFlips(),4),true);}
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.getPhase() == 0 && !grill.isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.grill_need_oil"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.getPhase() == 2 && !grill.isSeasoned()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.grill_need_seasoning"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canExtract()) {
            if (!level.isClientSide) {int extracted=player.isShiftKeyDown()?grill.extractAll(player):(grill.extractOne(player)?1:0);if(extracted>0)level.playSound(null,pos,ModSounds.PICKUP_ITEM.get(),SoundSource.BLOCKS,0.8F,1.0F);}
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){if(!s.getValue(LIT))return;if(r.nextInt(3)==0)l.addParticle(ParticleTypes.SMOKE,p.getX()+0.25+r.nextDouble()*0.5,p.getY()+0.22,p.getZ()+0.25+r.nextDouble()*0.5,0,0.02,0);if(r.nextInt(8)==0)l.addParticle(ParticleTypes.FLAME,p.getX()+0.3+r.nextDouble()*0.4,p.getY()+0.18,p.getZ()+0.3+r.nextDouble()*0.4,0,0.01,0);}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock())&&l.getBlockEntity(p)instanceof GrillBlockEntity grill)grill.dropForBreak();super.onRemove(s,l,p,n,moving);}
}
