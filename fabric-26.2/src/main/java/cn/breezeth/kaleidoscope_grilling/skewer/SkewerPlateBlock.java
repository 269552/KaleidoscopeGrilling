package cn.breezeth.kaleidoscope_grilling.skewer;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Faithful 26.2 port of the original five-skewer serving plate. */
public final class SkewerPlateBlock extends BaseEntityBlock {
  public static final MapCodec<SkewerPlateBlock> CODEC = simpleCodec(SkewerPlateBlock::new);
  public static final Property<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
  private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 7, 15);

  public SkewerPlateBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
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
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  protected InteractionResult useItemOn(
      ItemStack held,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof SkewerPlateBlockEntity plate)) {
      return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    if (held.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
    if (!SkewerPlateItem.isSkewer(held)) return InteractionResult.CONSUME;
    if (player.isShiftKeyDown() && hand != InteractionHand.MAIN_HAND) {
      return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    boolean canAdd = plate.size() < SkewerPlateBlockEntity.CAPACITY;
    if (!level.isClientSide() && plate.add(held, player.getAbilities().instabuild)) {
      level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
    }
    return canAdd ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof SkewerPlateBlockEntity plate) || plate.size() == 0) {
      return InteractionResult.PASS;
    }
    if (!level.isClientSide()) {
      ItemStack removed = plate.removeLast();
      if (!removed.isEmpty()) {
        if (player.getMainHandItem().isEmpty()) player.setItemInHand(InteractionHand.MAIN_HAND, removed);
        else player.getInventory().placeItemBackInInventory(removed);
      }
      level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (!(blockEntity instanceof SkewerPlateBlockEntity plate) || plate.size() == 0) return List.of();
    return List.of(SkewerPlateItem.create(plate.copySkewers()));
  }
}
