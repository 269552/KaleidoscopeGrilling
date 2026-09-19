package cn.breezeth.kaleidoscope_grilling.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PepperLogBlock extends RotatedPillarBlock {
  public static final MapCodec<PepperLogBlock> CODEC = simpleCodec(PepperLogBlock::new);

  public PepperLogBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  public static PepperLogBlock create() {
    return new PepperLogBlock(BlockBehaviour.Properties.ofLegacyCopy(Blocks.OAK_LOG));
  }

  @Override
  public MapCodec<? extends RotatedPillarBlock> codec() {
    return CODEC;
  }
}
