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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public final class SkewerRecipeBlock extends BaseEntityBlock {
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  private static final VoxelShape NORTH = Block.box(3, 1.5, 15.75, 13, 14.5, 16);
  private static final VoxelShape SOUTH = Block.box(3, 1.5, 0, 13, 14.5, 0.25);
  private static final VoxelShape WEST = Block.box(15.75, 1.5, 3, 16, 14.5, 13);
  private static final VoxelShape EAST = Block.box(0, 1.5, 3, 0.25, 14.5, 13);

  public SkewerRecipeBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SkewerRecipeBlockEntity(pos, state);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return switch (state.getValue(FACING)) {
      case SOUTH -> SOUTH;
      case WEST -> WEST;
      case EAST -> EAST;
      default -> NORTH;
    };
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    Direction facing = state.getValue(FACING);
    BlockPos support = pos.relative(facing.getOpposite());
    return level.getBlockState(support).isFaceSturdy(level, support, facing);
  }

  @Override
  public BlockState updateShape(
      BlockState state,
      Direction direction,
      BlockState neighbor,
      LevelAccessor level,
      BlockPos pos,
      BlockPos neighborPos) {
    return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
        ? Blocks.AIR.defaultBlockState()
        : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    ItemStack held = player.getItemInHand(hand);
    if (held.is(Items.STICK) && level.getBlockEntity(pos) instanceof SkewerRecipeBlockEntity recipe)
      return SkewerRecipeBookItem.craft(level, player, held, recipe.recipeBook());
    if (!held.isEmpty()) return InteractionResult.PASS;
    if (!level.isClientSide
        && level.getBlockEntity(pos) instanceof SkewerRecipeBlockEntity recipe) {
      ItemStack book = recipe.recipeBook();
      level.removeBlock(pos, false);
      if (!player.addItem(book)) player.drop(book, false);
      level.playSound(
          null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    return blockEntity instanceof SkewerRecipeBlockEntity recipe
        ? List.of(recipe.recipeBook())
        : List.of();
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }
}
