package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class HouttuyniaCropBlock extends CropBlock {
  public static final MapCodec<HouttuyniaCropBlock> CODEC = simpleCodec(HouttuyniaCropBlock::new);
  public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
  public static final BooleanProperty RED_VARIANT = BooleanProperty.create("red_variant");

  public HouttuyniaCropBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(RED_VARIANT, false));
  }

  @Override
  public MapCodec<? extends CropBlock> codec() {
    return CODEC;
  }

  @Override
  public IntegerProperty getAgeProperty() {
    return AGE;
  }

  @Override
  public int getMaxAge() {
    return 7;
  }

  @Override
  protected Item getBaseSeedId() {
    return ModItems.HOUTTUYNIA.get();
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = super.getStateForPlacement(context);
    if (state == null) return null;
    boolean soulSand =
        context.getLevel().getBlockState(context.getClickedPos().below()).is(Blocks.SOUL_SAND);
    return state.setValue(RED_VARIANT, soulSand || context.getLevel().random.nextFloat() < 0.3F);
  }

  @Override
  protected void randomTick(
      BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    super.randomTick(state, level, pos, random);
    preserveVariant(level, pos, state);
  }

  @Override
  public void growCrops(Level level, BlockPos pos, BlockState state) {
    super.growCrops(level, pos, state);
    preserveVariant(level, pos, state);
  }

  private static void preserveVariant(Level level, BlockPos pos, BlockState previousState) {
    BlockState current = level.getBlockState(pos);
    if (!current.hasProperty(RED_VARIANT)) return;
    boolean red =
        level.getBlockState(pos.below()).is(Blocks.SOUL_SAND)
            || previousState.getValue(RED_VARIANT);
    if (current.getValue(RED_VARIANT) != red) {
      level.setBlock(pos, current.setValue(RED_VARIANT, red), 2);
    }
  }

  @Override
  protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    BlockState ground = level.getBlockState(pos.below());
    return ground.is(Blocks.SOUL_SAND)
        || (ground.getBlock() instanceof FarmBlock && level.getRawBrightness(pos, 0) >= 8);
  }

  @Override
  protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
    return state.getBlock() instanceof FarmBlock || state.is(Blocks.SOUL_SAND);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(AGE, RED_VARIANT);
  }
}
