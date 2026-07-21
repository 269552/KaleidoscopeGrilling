package cn.breezeth.kaleidoscope_grilling;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public final class PremiumChiliOilBlock extends LiquidBlock {
  public PremiumChiliOilBlock(
      Supplier<? extends FlowingFluid> fluid, BlockBehaviour.Properties properties) {
    super(fluid, properties);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    super.animateTick(state, level, pos, random);
    if (random.nextInt(5) != 0) return;
    double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
    double y = pos.getY() + 0.75D + random.nextDouble() * 0.2D;
    double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
    level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0D, 0.05D, 0.0D);
    if (random.nextBoolean()) level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.035D, 0.0D);
  }
}
