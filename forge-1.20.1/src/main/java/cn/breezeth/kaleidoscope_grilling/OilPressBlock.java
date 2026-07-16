package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import javax.annotation.Nullable;

public final class OilPressBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty CAKE_COUNT = IntegerProperty.create("cake_count", 0, OilPressBlockEntity.MAX_CAKES);
    public static final IntegerProperty PRESS_STAGE = IntegerProperty.create("press_stage", 0, 4);
    public OilPressBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CAKE_COUNT, 0).setValue(PRESS_STAGE, 0)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, CAKE_COUNT, PRESS_STAGE); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OilPressBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.OIL_PRESS.get(), OilPressBlockEntity::tick);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof OilPressBlockEntity press)) return InteractionResult.PASS;
        if (press.waitingForContainer()) {
            if (!level.isClientSide) press.inspectForContainer();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModItems.OIL_CAKE.get())) {
            if (!level.isClientSide) {
                if (press.addCake()) { if (!player.getAbilities().instabuild) held.shrink(1); level.playSound(null,pos,SoundEvents.GRASS_PLACE,SoundSource.BLOCKS,0.8F,1.0F); }
                else player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.press_full"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        int toolProgress = OilPressTools.progress(held);
        if (toolProgress > 0) {
            if (!level.isClientSide && press.cakes() < OilPressBlockEntity.MAX_CAKES)
                player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.press_need_full_batch", press.cakes(), OilPressBlockEntity.MAX_CAKES), true);
            if (!level.isClientSide && press.pressWithTool(player, toolProgress)) {
                AnvilPressAnimation.start(player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && press.residue() > 0) {
            if (!level.isClientSide) player.addItem(new ItemStack(ModItems.OIL_RESIDUE.get(), press.takeResidue()));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.press_status",
                press.cakes(), press.progress(), OilPressBlockEntity.REQUIRED_PROGRESS), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack out = new ItemStack(ModBlocks.OIL_PRESS_ITEM.get());
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof OilPressBlockEntity press) {
            CompoundTag data = new CompoundTag(); press.saveAdditional(data); out.addTagElement("BlockEntityTag", data);
        }
        return List.of(out);
    }
}
