package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class SweetPotatoCropBlock extends CropBlock {
  public static final MapCodec<SweetPotatoCropBlock> CODEC = simpleCodec(SweetPotatoCropBlock::new);
  public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

  public SweetPotatoCropBlock(Properties p) {
    super(p);
    registerDefaultState(stateDefinition.any().setValue(AGE, 0));
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
    return ModItems.SWEET_POTATO.get();
  }

  @Override
  public void randomTick(
      BlockState s,
      net.minecraft.server.level.ServerLevel l,
      net.minecraft.core.BlockPos p,
      net.minecraft.util.RandomSource r) {
    super.randomTick(s, l, p, r);
  }

  @Override
  protected void createBlockStateDefinition(
      StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> b) {
    b.add(AGE);
  }
}
