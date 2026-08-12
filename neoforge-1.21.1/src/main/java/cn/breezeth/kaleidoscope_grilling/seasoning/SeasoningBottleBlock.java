package cn.breezeth.kaleidoscope_grilling.seasoning;

import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class SeasoningBottleBlock extends BaseEntityBlock {
  public static final MapCodec<SeasoningBottleBlock> CODEC = simpleCodec(SeasoningBottleBlock::new);
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 4);
  private static final net.minecraft.world.phys.shapes.VoxelShape[] SHAPES = {
    Block.box(5, 0, 5, 11, 12.25, 11), Block.box(2, 0, 4, 15, 12.25, 14),
    Block.box(2, 0, 1.25, 15, 12.25, 15), Block.box(0.75, 0, 1.75, 15, 12.25, 16)
  };

  public SeasoningBottleBlock(Properties properties) {
    super(properties);
    registerDefaultState(
        stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COUNT, 1));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SeasoningBottleBlockEntity(pos, state);
  }

  @Override
  protected net.minecraft.world.phys.shapes.VoxelShape getShape(
      BlockState state,
      net.minecraft.world.level.BlockGetter level,
      BlockPos pos,
      net.minecraft.world.phys.shapes.CollisionContext context) {
    return SHAPES[state.getValue(COUNT) - 1];
  }

  @Override
  public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
    return context.getClickedFace() == Direction.UP
            && defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
        ? defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
        : null;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, COUNT);
  }

  @Override
  protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
  }

  @Override
  protected BlockState updateShape(
      BlockState state,
      Direction direction,
      BlockState neighbor,
      LevelAccessor level,
      BlockPos pos,
      BlockPos neighborPos) {
    return direction == Direction.DOWN && !state.canSurvive(level, pos)
        ? Blocks.AIR.defaultBlockState()
        : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
  }

  @Override
  protected ItemInteractionResult useItemOn(
      ItemStack held,
      BlockState state,
      Level level,
      BlockPos pos,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof SeasoningBottleBlockEntity bottle))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (held.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (SeasoningBottleBlockEntity.isBottle(held)) {
      if (!level.isClientSide && bottle.push(held)) {
        if (!player.getAbilities().instabuild) held.shrink(1);
        level.playSound(
            null, pos, ModSounds.SEASONING_BOTTLE_STACK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
      }
      return ItemInteractionResult.SUCCESS;
    }
    ResourceLocation id = BuiltInRegistries.ITEM.getKey(held.getItem());
    if (bottle.canAdd(id.toString())) {
      if (!level.isClientSide) {
        bottle.add(id.toString());
        ModAdvancements.seasoningAdded(player, bottle.ingredients());
        if (!player.getAbilities().instabuild) held.shrink(1);
        level.playSound(null, pos, ModSounds.ACTION_SUCCESS.get(), SoundSource.BLOCKS, 0.65F, 1.0F);
        if (level instanceof net.minecraft.server.level.ServerLevel server)
          server.sendParticles(
              ParticleTypes.END_ROD,
              pos.getX() + 0.5,
              pos.getY() + 0.7,
              pos.getZ() + 0.5,
              5,
              0.12,
              0.12,
              0.12,
              0.01);
      }
      return ItemInteractionResult.SUCCESS;
    }
    if (!level.isClientSide)
      player.displayClientMessage(
          Component.translatable(
              bottle.ingredients().size() >= 8
                  ? "message.kaleidoscope_grilling.bottle_full"
                  : "message.kaleidoscope_grilling.invalid_seasoning"),
          true);
    return ItemInteractionResult.SUCCESS;
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof SeasoningBottleBlockEntity bottle))
      return InteractionResult.PASS;
    if (!level.isClientSide) pickupOne(level, pos, player, bottle);
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  private static void pickupOne(
      Level level, BlockPos pos, Player player, SeasoningBottleBlockEntity bottle) {
    ItemStack result = bottle.pop();
    if (result.isEmpty()) return;
    if (bottle.count() == 0) level.removeBlock(pos, false);
    player.setItemInHand(InteractionHand.MAIN_HAND, result);
    level.playSound(
        null, pos, ModSounds.SEASONING_BOTTLE_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    warnMissingBase(player, result);
  }

  @Override
  public void setPlacedBy(
      Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (level.getBlockEntity(pos) instanceof SeasoningBottleBlockEntity bottle)
      bottle.loadFrom(stack);
  }

  @Override
  public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (!level.isClientSide
        && level.getBlockEntity(pos) instanceof SeasoningBottleBlockEntity bottle) {
      for (ItemStack stack : bottle.bottles()) {
        Block.popResource(level, pos, stack);
        warnMissingBase(player, stack);
      }
    }
    return super.playerWillDestroy(level, pos, state, player);
  }

  private static void warnMissingBase(Player player, ItemStack stack) {
    List<String> ingredients = SeasoningData.get(stack);
    if (!ingredients.isEmpty()
        && !(ingredients.contains("kaleidoscope_grilling:green_chili_powder")
            && ingredients.contains("kaleidoscope_grilling:sichuan_pepper")
            && ingredients.contains("kaleidoscope_grilling:onion_powder")))
      player.displayClientMessage(
          Component.translatable("message.kaleidoscope_grilling.missing_base_seasoning"), false);
  }
}
