package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class SkewerPlateBlock extends BaseEntityBlock {
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 7, 15);

  public SkewerPlateBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SkewerPlateBlockEntity(pos, state);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof SkewerPlateBlockEntity plate))
      return InteractionResult.PASS;
    ItemStack held = player.getItemInHand(hand);
    if (SkewerPlateItem.isSkewer(held)) {
      if (player.isShiftKeyDown() && hand != InteractionHand.MAIN_HAND)
        return InteractionResult.PASS;
      if (!level.isClientSide && plate.add(held, player.getAbilities().instabuild))
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
      return plate.size() < SkewerPlateBlockEntity.CAPACITY || level.isClientSide
          ? InteractionResult.sidedSuccess(level.isClientSide)
          : InteractionResult.CONSUME;
    }
    if (!held.isEmpty()) return InteractionResult.PASS;
    if (plate.size() == 0) return InteractionResult.PASS;
    if (!level.isClientSide) {
      ItemStack removed = plate.removeLast();
      if (!removed.isEmpty()) player.setItemInHand(hand, removed);
      level.playSound(
          null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (!(blockEntity instanceof SkewerPlateBlockEntity plate) || plate.size() == 0)
      return List.of();
    return List.of(SkewerPlateItem.create(plate.copySkewers()));
  }
}
