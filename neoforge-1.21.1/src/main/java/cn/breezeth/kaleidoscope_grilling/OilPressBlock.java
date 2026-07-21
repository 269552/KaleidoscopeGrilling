package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class OilPressBlock extends BaseEntityBlock {
  public static final MapCodec<OilPressBlock> CODEC = simpleCodec(OilPressBlock::new);
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  public static final IntegerProperty CAKE_COUNT =
      IntegerProperty.create("cake_count", 0, OilPressBlockEntity.MAX_CAKES);
  public static final IntegerProperty PRESS_STAGE = IntegerProperty.create("press_stage", 0, 4);

  public OilPressBlock(Properties properties) {
    super(properties);
    registerDefaultState(
        stateDefinition
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(CAKE_COUNT, 0)
            .setValue(PRESS_STAGE, 0));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, CAKE_COUNT, PRESS_STAGE);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new OilPressBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide
        ? null
        : createTickerHelper(type, ModBlockEntities.OIL_PRESS.get(), OilPressBlockEntity::tick);
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
    if (!(level.getBlockEntity(pos) instanceof OilPressBlockEntity press))
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    if (press.waitingForContainer()) {
      if (!level.isClientSide) press.inspectForContainer();
      return ItemInteractionResult.SUCCESS;
    }
    if (held.is(ModItems.OIL_CAKE.get())) {
      if (!level.isClientSide) {
        if (press.addCake()) {
          if (!player.getAbilities().instabuild) held.shrink(1);
          level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
        } else
          player.displayClientMessage(
              Component.translatable("message.kaleidoscope_grilling.press_full"), true);
      }
      return ItemInteractionResult.SUCCESS;
    }
    int toolProgress = OilPressTools.progress(held);
    if (toolProgress > 0) {
      if (!level.isClientSide && press.cakes() < OilPressBlockEntity.MAX_CAKES)
        player.displayClientMessage(
            Component.translatable(
                "message.kaleidoscope_grilling.press_need_full_batch",
                press.cakes(),
                OilPressBlockEntity.MAX_CAKES),
            true);
      if (!level.isClientSide && press.pressWithTool(player, toolProgress)) {
        AnvilPressAnimation.start(player);
      }
      return ItemInteractionResult.SUCCESS;
    }
    if (held.isEmpty() && press.residue() > 0) {
      if (!level.isClientSide)
        player.addItem(new ItemStack(ModItems.OIL_RESIDUE.get(), press.takeResidue()));
      return ItemInteractionResult.SUCCESS;
    }
    if (!level.isClientSide)
      player.displayClientMessage(
          Component.translatable(
              "message.kaleidoscope_grilling.press_status",
              press.cakes(),
              press.progress(),
              OilPressBlockEntity.REQUIRED_PROGRESS),
          true);
    return ItemInteractionResult.SUCCESS;
  }

  @Override
  protected net.minecraft.world.InteractionResult useWithoutItem(
      BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (!(level.getBlockEntity(pos) instanceof OilPressBlockEntity press))
      return net.minecraft.world.InteractionResult.PASS;
    if (!level.isClientSide) {
      OilPressBlockEntity.InspectionResult result = press.inspectForContainer();
      if (result == OilPressBlockEntity.InspectionResult.NOT_WAITING)
        player.displayClientMessage(
            Component.translatable(
                "message.kaleidoscope_grilling.press_status",
                press.cakes(),
                press.progress(),
                OilPressBlockEntity.REQUIRED_PROGRESS),
            true);
    }
    return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
  }

  @Override
  protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
    ItemStack out = new ItemStack(ModBlocks.OIL_PRESS_ITEM.get());
    BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (blockEntity instanceof OilPressBlockEntity press) {
      CompoundTag data = new CompoundTag();
      press.saveAdditional(data, params.getLevel().registryAccess());
      data.putString("id", KaleidoscopeGrilling.MOD_ID + ":oil_press");
      out.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(data));
    }
    return List.of(out);
  }
}
