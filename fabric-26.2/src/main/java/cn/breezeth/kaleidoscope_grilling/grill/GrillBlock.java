package cn.breezeth.kaleidoscope_grilling.grill;

import cn.breezeth.kaleidoscope_grilling.GrillAutomationApi;
import cn.breezeth.kaleidoscope_grilling.oil.OilBrushAnimation;
import cn.breezeth.kaleidoscope_grilling.oil.OilPotCompat;
import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlockEntities;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningAnimation;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class GrillBlock extends BaseEntityBlock {
  public static final MapCodec<GrillBlock> CODEC = simpleCodec(GrillBlock::new);
  public static final BooleanProperty LEGGED = BooleanProperty.create("legged");
  public static final BooleanProperty LIT = BooleanProperty.create("lit");
  public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
      HorizontalDirectionalBlock.FACING;
  private static final TagKey<Item> EXTINGUISH_TOOLS = TagKey.create(
      Registries.ITEM, Identifier.fromNamespaceAndPath("kaleidoscope_cookery", "extinguish_stove"));

  private static final VoxelShape FLAT_Z = Block.box(0, 0, 2, 16, 4, 14);
  private static final VoxelShape FLAT_X = Block.box(2, 0, 0, 14, 4, 16);
  private static final VoxelShape LEGS_Z = Shapes.or(FLAT_Z,
      Block.box(0, 0, 2, 2, 4, 4), Block.box(14, 0, 2, 16, 4, 4),
      Block.box(0, 0, 12, 2, 4, 14), Block.box(14, 0, 12, 16, 4, 14));
  private static final VoxelShape LEGS_X = Shapes.or(FLAT_X,
      Block.box(2, 0, 0, 4, 4, 2), Block.box(12, 0, 0, 14, 4, 2),
      Block.box(2, 0, 14, 4, 4, 16), Block.box(12, 0, 14, 14, 4, 16));

  public GrillBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(LEGGED, false).setValue(LIT, false)
        .setValue(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(LEGGED, LIT, FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockPos below = context.getClickedPos().below();
    return defaultBlockState()
        .setValue(LEGGED, !context.getLevel().getBlockState(below)
            .isFaceSturdy(context.getLevel(), below, Direction.UP))
        .setValue(LIT, false)
        .setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
      BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
      RandomSource random) {
    if (direction == Direction.DOWN) {
      return state.setValue(LEGGED, !neighborState.isFaceSturdy(level, neighborPos, Direction.UP));
    }
    return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    boolean x = state.getValue(FACING).getAxis() == Direction.Axis.X;
    return state.getValue(LEGGED) ? (x ? LEGS_X : LEGS_Z) : (x ? FLAT_X : FLAT_Z);
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new GrillBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide() ? null
        : createTickerHelper(type, ModBlockEntities.GRILL.get(), GrillBlockEntity::tick);
  }

  @Override
  protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
      Player player, InteractionHand hand, BlockHitResult hit) {
    if (stack.is(Items.FLINT_AND_STEEL)) {
      if (!state.getValue(LIT)) {
        if (!level.isClientSide()) {
          GrillAutomationApi.ignite(level, pos, stack, player,
              hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND,
              false);
        }
        return InteractionResult.SUCCESS;
      }
      return InteractionResult.CONSUME;
    }

    if (stack.is(EXTINGUISH_TOOLS) && state.getValue(LIT)) {
      if (!level.isClientSide()) {
        if (player.isShiftKeyDown()) GrillAutomationApi.forceUnlock(level, pos);
        GrillAutomationApi.extinguish(level, pos, false);
      }
      return InteractionResult.SUCCESS;
    }

    if (level.getBlockEntity(pos) instanceof GrillBlockEntity && OilPotCompat.isOilPot(stack)) {
      GrillAutomationApi.Result preview = GrillAutomationApi.brushOil(level, pos, stack, true);
      if (preview.success() && !level.isClientSide()) {
        String oilType = OilPotCompat.getType(stack);
        GrillAutomationApi.Result result = GrillAutomationApi.brushOil(level, pos, stack, false);
        if (result.success()) {
          OilBrushAnimation.start(player, hand, oilType);
          ModAdvancements.oiled(player);
          level.playSound(null, pos, ModSounds.GRILL_FLIP.get(), SoundSource.BLOCKS, 0.75F, 1.0F);
          message(player, Component.translatable("message.kaleidoscope_grilling.oiled", result.affected()));
        }
      } else if (!level.isClientSide()) {
        message(player, Component.translatable(
            preview.status() == GrillAutomationApi.Status.INSUFFICIENT_RESOURCE
                ? "message.kaleidoscope_grilling.not_enough_oil"
                : "message.kaleidoscope_grilling.no_brushable_skewers"));
      }
      return InteractionResult.SUCCESS;
    }

    if (level.getBlockEntity(pos) instanceof GrillBlockEntity && stack.is(ModItems.SPECIAL_SEASONING.get())) {
      GrillAutomationApi.Result preview = GrillAutomationApi.season(level, pos, stack, true);
      if (preview.success() && !level.isClientSide()) {
        GrillAutomationApi.Result result = GrillAutomationApi.season(level, pos, stack, false);
        if (result.success()) {
          SeasoningAnimation.start(player);
          level.playSound(null, pos, ModSounds.SEASON.get(), SoundSource.BLOCKS, 0.85F, 1.0F);
          message(player, Component.translatable("message.kaleidoscope_grilling.grill_ready_to_take")
              .withStyle(ChatFormatting.RED));
          if (result.shouldReplaceHeld()) player.setItemInHand(hand, result.heldReplacement());
        }
      } else if (!level.isClientSide()) {
        message(player, Component.translatable(
            preview.status() == GrillAutomationApi.Status.INSUFFICIENT_RESOURCE
                ? "message.kaleidoscope_grilling.not_enough_seasoning"
                : "message.kaleidoscope_grilling.no_seasonable_skewers"));
      }
      return InteractionResult.SUCCESS;
    }

    if (level.getBlockEntity(pos) instanceof GrillBlockEntity) {
      GrillAutomationApi.Result preview = GrillAutomationApi.insertSkewer(
          level, pos, stack, true, !player.getAbilities().instabuild);
      if (preview.status() == GrillAutomationApi.Status.NOT_LIT) {
        if (!level.isClientSide()) message(player,
            Component.translatable("message.kaleidoscope_grilling.grill_need_heat"));
        return InteractionResult.SUCCESS;
      }
      if (preview.success()) {
        if (!level.isClientSide() && GrillAutomationApi.insertSkewer(
            level, pos, stack, false, !player.getAbilities().instabuild).success()) {
          level.playSound(null, pos, ModSounds.ACTION_SUCCESS.get(), SoundSource.BLOCKS, 0.65F, 1.0F);
        }
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.TRY_WITH_EMPTY_HAND;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
      Player player, BlockHitResult hit) {
    if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill
        && GrillAutomationApi.flip(level, pos, true).success()) {
      if (!level.isClientSide() && GrillAutomationApi.flip(level, pos, false).success()) {
        level.playSound(null, pos, ModSounds.GRILL_FLIP.get(), SoundSource.BLOCKS, 0.75F, 1.0F);
        message(player, Component.translatable(
            "message.kaleidoscope_grilling.grill_wait_flip", grill.getFlips(), 4));
      }
      return InteractionResult.SUCCESS;
    }
    if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.getPhase() == 0 && !grill.isEmpty()) {
      if (!level.isClientSide()) message(player,
          Component.translatable("message.kaleidoscope_grilling.grill_need_oil"));
      return InteractionResult.SUCCESS;
    }
    if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.getPhase() == 2 && !grill.isSeasoned()) {
      if (!level.isClientSide()) message(player,
          Component.translatable("message.kaleidoscope_grilling.grill_need_seasoning"));
      return InteractionResult.SUCCESS;
    }
    if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canExtract()) {
      if (!level.isClientSide()) {
        int extracted = 0;
        do {
          GrillAutomationApi.Result result = GrillAutomationApi.extract(level, pos, false);
          if (!result.success()) break;
          player.getInventory().placeItemBackInInventory(result.output());
          extracted++;
        } while (player.isShiftKeyDown());
        if (extracted > 0) {
          level.playSound(null, pos, ModSounds.PICKUP_ITEM.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
        }
      }
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  private static void message(Player player, Component component) {
    player.sendSystemMessage(component);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (!state.getValue(LIT)) return;
    if (random.nextInt(3) == 0) {
      level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25 + random.nextDouble() * 0.5,
          pos.getY() + 0.22, pos.getZ() + 0.25 + random.nextDouble() * 0.5, 0, 0.02, 0);
    }
    if (random.nextInt(8) == 0) {
      level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.3 + random.nextDouble() * 0.4,
          pos.getY() + 0.18, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.01, 0);
    }
  }

  @Override
  protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
    if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof GrillBlockEntity grill) {
      grill.dropForBreak();
    }
    super.onRemove(state, level, pos, newState, moving);
  }
}
