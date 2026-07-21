package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class CanolaCropBlock extends CropBlock {
  public static final IntegerProperty AGE =
      net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7;

  public CanolaCropBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(AGE, 0));
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
    return ModItems.CANOLA_SEEDS.get();
  }

  @Override
  public void randomTick(
      BlockState state,
      net.minecraft.server.level.ServerLevel level,
      net.minecraft.core.BlockPos pos,
      net.minecraft.util.RandomSource random) {
    super.randomTick(state, level, pos, random);
  }

  @Override
  protected int getBonemealAgeIncrease(net.minecraft.world.level.Level level) {
    return 1;
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
    builder.add(AGE);
  }
}
