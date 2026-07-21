package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PepperLogBlock extends RotatedPillarBlock {
  public PepperLogBlock() {
    super(BlockBehaviour.Properties.copy(Blocks.OAK_LOG));
  }
}
