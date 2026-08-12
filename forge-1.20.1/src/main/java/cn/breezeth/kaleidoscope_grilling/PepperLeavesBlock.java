package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PepperLeavesBlock extends LeavesBlock {
  public static final BooleanProperty HAS_PEPPER = BooleanProperty.create("has_pepper");
  private static final String LAST_STING_TAG = "KaleidoscopeGrillingPepperSting";
  private static final VoxelShape TOP_COLLISION_SHAPE = Block.box(0, 15, 0, 16, 16, 16);
  private static final int STING_INTERVAL_TICKS = 20;

  public PepperLeavesBlock() {
    super(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).randomTicks());
    registerDefaultState(defaultBlockState().setValue(HAS_PEPPER, false));
  }

  @Override
  public void setPlacedBy(
      Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    level.setBlock(pos, state.setValue(PERSISTENT, true), 3);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(HAS_PEPPER);
  }

  @Override
  public boolean isRandomlyTicking(BlockState state) {
    return !state.getValue(HAS_PEPPER) || super.isRandomlyTicking(state);
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (!state.getValue(HAS_PEPPER) && random.nextInt(20) == 0) {
      level.setBlock(pos, state.setValue(HAS_PEPPER, true), 3);
    }
    super.randomTick(state, level, pos, random);
  }

  @Override
  public VoxelShape getCollisionShape(
      BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return context.isAbove(TOP_COLLISION_SHAPE, pos, true)
        ? TOP_COLLISION_SHAPE
        : Shapes.empty();
  }

  @Override
  public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    if (!(entity instanceof LivingEntity)
        || entity.getType() == EntityType.FOX
        || entity.getType() == EntityType.BEE) return;
    entity.makeStuckInBlock(state, new Vec3(0.8, 0.75, 0.8));
    sting(level, entity);
  }

  @Override
  public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    sting(level, entity);
    super.stepOn(level, pos, state, entity);
  }

  private static void sting(Level level, Entity entity) {
    if (level.isClientSide
        || !(entity instanceof LivingEntity)
        || entity.getType() == EntityType.FOX
        || entity.getType() == EntityType.BEE) return;
    long now = level.getGameTime();
    long lastSting = entity.getPersistentData().getLong(LAST_STING_TAG);
    if (now - lastSting < STING_INTERVAL_TICKS) return;
    if (entity.hurt(level.damageSources().sweetBerryBush(), 1.0F)) {
      entity.getPersistentData().putLong(LAST_STING_TAG, now);
    }
  }

  @Override
  public InteractionResult use(
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (state.getValue(HAS_PEPPER)) {
      if (!level.isClientSide) {
        popResource(
            level, pos, new ItemStack(ModItems.SICHUAN_PEPPER.get(), 1 + level.random.nextInt(2)));
        ModAdvancements.pepperPicked(player);
        level.setBlock(pos, state.setValue(HAS_PEPPER, false), 3);
        level.playSound(
            null,
            pos,
            SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
            SoundSource.BLOCKS,
            1.0F,
            0.8F + level.random.nextFloat() * 0.4F);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
    }
    return InteractionResult.PASS;
  }
}
