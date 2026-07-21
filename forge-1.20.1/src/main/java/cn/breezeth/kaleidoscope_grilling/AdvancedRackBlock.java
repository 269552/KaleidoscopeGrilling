package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class AdvancedRackBlock extends BaseEntityBlock {
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty SPICE_LEVEL = IntegerProperty.create("spice_level", 0, 4);
  private static final VoxelShape NORTH = Block.box(1, 5, 11, 15, 14, 16);
  private static final VoxelShape SOUTH = Block.box(1, 5, 0, 15, 14, 5);
  private static final VoxelShape EAST = Block.box(0, 5, 1, 5, 14, 15);
  private static final VoxelShape WEST = Block.box(11, 5, 1, 16, 14, 15);

  public AdvancedRackBlock(Properties properties) {
    super(properties);
    registerDefaultState(
        stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(SPICE_LEVEL, 0));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, SPICE_LEVEL);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new AdvancedRackBlockEntity(pos, state);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return switch (state.getValue(FACING)) {
      case NORTH -> NORTH;
      case SOUTH -> SOUTH;
      case EAST -> EAST;
      case WEST -> WEST;
      default -> NORTH;
    };
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack))
      return InteractionResult.PASS;
    if (!level.isClientSide) player.openMenu(rack);
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public void onRemove(
      BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
    if (!oldState.is(newState.getBlock())) level.updateNeighbourForOutputSignal(pos, this);
    super.onRemove(oldState, level, pos, newState, movedByPiston);
  }

  @Override
  public void setPlacedBy(
      Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    CompoundTag data = stack.getTagElement("BlockEntityTag");
    if (data != null && level.getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack) {
      rack.restoreFromItem(data);
    }
  }

  @Override
  public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    ItemStack dropped = new ItemStack(ModBlocks.ADVANCED_RACK_ITEM.get());
    BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (blockEntity instanceof AdvancedRackBlockEntity rack) {
      CompoundTag data = new CompoundTag();
      rack.saveAdditional(data);
      dropped.addTagElement("BlockEntityTag", data);
    }
    return List.of(dropped);
  }
}
