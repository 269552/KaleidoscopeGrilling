package cn.breezeth.kaleidoscope_grilling.world;

import cn.breezeth.kaleidoscope_grilling.registry.ModFeatures;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PepperSaplingBlock extends BushBlock implements BonemealableBlock {
  private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
  public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);

  public PepperSaplingBlock() {
    super(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING));
    registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(STAGE);
  }

  @Override
  public VoxelShape getShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public boolean isValidBonemealTarget(
      LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
    return true;
  }

  @Override
  public boolean isBonemealSuccess(
      Level level, RandomSource random, BlockPos pos, BlockState state) {
    return level.random.nextFloat() < 0.45F;
  }

  @Override
  public void performBonemeal(
      ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    advanceTree(level, pos, state, random);
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
      advanceTree(level, pos, state, random);
    }
  }

  private void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
    if (state.getValue(STAGE) == 0) {
      level.setBlock(pos, state.cycle(STAGE), 4);
    } else {
      level.removeBlock(pos, false);
      boolean placed =
          ModFeatures.PEPPER_TREE
              .get()
              .place(
                  new FeaturePlaceContext<>(
                      Optional.empty(),
                      level,
                      level.getChunkSource().getGenerator(),
                      random,
                      pos,
                      NoneFeatureConfiguration.INSTANCE));
      if (!placed) {
        level.setBlock(pos, state, 4);
      }
    }
  }
}
