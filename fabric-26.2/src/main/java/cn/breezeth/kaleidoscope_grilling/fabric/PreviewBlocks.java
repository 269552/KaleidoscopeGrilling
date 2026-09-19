package cn.breezeth.kaleidoscope_grilling.fabric;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Lightweight 26.2 block ports used while the original block entities are moved to Fabric. */
final class PreviewBlocks {
    private PreviewBlocks() {}

    static final class Grill extends HorizontalDirectionalBlock {
        static final BooleanProperty LEGGED = BooleanProperty.create("legged");
        static final BooleanProperty LIT = BlockStateProperties.LIT;
        static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

        private static final VoxelShape FLAT_Z = Block.box(0, 0, 2, 16, 4, 14);
        private static final VoxelShape FLAT_X = Block.box(2, 0, 0, 14, 4, 16);
        private static final VoxelShape LEGS_Z = Shapes.or(
                FLAT_Z,
                Block.box(0, 0, 2, 2, 4, 4),
                Block.box(14, 0, 2, 16, 4, 4),
                Block.box(0, 0, 12, 2, 4, 14),
                Block.box(14, 0, 12, 16, 4, 14));
        private static final VoxelShape LEGS_X = Shapes.or(
                FLAT_X,
                Block.box(2, 0, 0, 4, 4, 2),
                Block.box(12, 0, 0, 14, 4, 2),
                Block.box(2, 0, 14, 4, 4, 16),
                Block.box(12, 0, 14, 14, 4, 16));

        Grill(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any()
                    .setValue(LEGGED, false)
                    .setValue(LIT, false)
                    .setValue(FACING, Direction.NORTH));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return simpleCodec(Grill::new);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(LEGGED, LIT, FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            BlockPos below = context.getClickedPos().below();
            boolean supported = context.getLevel().getBlockState(below)
                    .isFaceSturdy(context.getLevel(), below, Direction.UP);
            return defaultBlockState()
                    .setValue(LEGGED, !supported)
                    .setValue(LIT, false)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            boolean x = state.getValue(FACING).getAxis() == Direction.Axis.X;
            return state.getValue(LEGGED) ? (x ? LEGS_X : LEGS_Z) : (x ? FLAT_X : FLAT_Z);
        }

        @Override
        public InteractionResult useItemOn(
                ItemStack stack,
                BlockState state,
                Level level,
                BlockPos pos,
                Player player,
                InteractionHand hand,
                BlockHitResult hit) {
            if (stack.is(Items.FLINT_AND_STEEL)) {
                if (!state.getValue(LIT)) {
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                        stack.hurtAndBreak(
                                1,
                                player,
                                hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    }
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            }

            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (state.getValue(LIT)
                    && PreviewContent.MOD_ID.equals(id.getNamespace())
                    && id.getPath().startsWith("raw_")
                    && id.getPath().endsWith("_skewer")) {
                Item cooked = PreviewContent.item("grilled_" + id.getPath().substring(4));
                if (cooked != null) {
                    if (!level.isClientSide()) {
                        if (!player.getAbilities().instabuild) stack.shrink(1);
                        player.getInventory().placeItemBackInInventory(new ItemStack(cooked));
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        @Override
        protected InteractionResult useWithoutItem(
                BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (player.isShiftKeyDown() && state.getValue(LIT)) {
                if (!level.isClientSide()) level.setBlockAndUpdate(pos, state.setValue(LIT, false));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }

    static final class AdvancedRack extends HorizontalDirectionalBlock {
        static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        static final IntegerProperty SPICE_LEVEL = IntegerProperty.create("spice_level", 0, 4);
        private static final VoxelShape NORTH = Block.box(1, 5, 11, 15, 14, 16);
        private static final VoxelShape SOUTH = Block.box(1, 5, 0, 15, 14, 5);
        private static final VoxelShape EAST = Block.box(0, 5, 1, 5, 14, 15);
        private static final VoxelShape WEST = Block.box(11, 5, 1, 16, 14, 15);

        AdvancedRack(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(SPICE_LEVEL, 0));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return simpleCodec(AdvancedRack::new);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, SPICE_LEVEL);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return switch (state.getValue(FACING)) {
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case EAST -> EAST;
                case WEST -> WEST;
                default -> NORTH;
            };
        }
    }

    static final class OilPress extends HorizontalDirectionalBlock {
        static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        static final IntegerProperty CAKE_COUNT = IntegerProperty.create("cake_count", 0, 4);
        static final IntegerProperty PRESS_STAGE = IntegerProperty.create("press_stage", 0, 4);

        OilPress(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any()
                    .setValue(FACING, Direction.NORTH)
                    .setValue(CAKE_COUNT, 0)
                    .setValue(PRESS_STAGE, 0));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return simpleCodec(OilPress::new);
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
        public InteractionResult useItemOn(
                ItemStack stack,
                BlockState state,
                Level level,
                BlockPos pos,
                Player player,
                InteractionHand hand,
                BlockHitResult hit) {
            if (stack.is(PreviewContent.item("oil_cake")) && state.getValue(CAKE_COUNT) < 4) {
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(pos, state.setValue(CAKE_COUNT, state.getValue(CAKE_COUNT) + 1));
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof AnvilBlock
                    && state.getValue(CAKE_COUNT) == 4
                    && state.getValue(PRESS_STAGE) < 4) {
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(pos, state.setValue(PRESS_STAGE, state.getValue(PRESS_STAGE) + 1));
                }
                return InteractionResult.SUCCESS;
            }
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        @Override
        protected InteractionResult useWithoutItem(
                BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.kaleidoscope_grilling.press_status",
                                state.getValue(CAKE_COUNT),
                                state.getValue(PRESS_STAGE) * 4,
                                16),
                        true);
            }
            return InteractionResult.SUCCESS;
        }
    }

    static final class SkewerPlate extends HorizontalDirectionalBlock {
        static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 7, 15);

        SkewerPlate(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return simpleCodec(SkewerPlate::new);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }
    }

    static final class SeasoningBottle extends HorizontalDirectionalBlock {
        static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 4);
        private static final VoxelShape[] SHAPES = {
                Block.box(5, 0, 5, 11, 12.25, 11),
                Block.box(2, 0, 4, 15, 12.25, 14),
                Block.box(2, 0, 1.25, 15, 12.25, 15),
                Block.box(0.75, 0, 1.75, 15, 12.25, 16)
        };

        SeasoningBottle(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COUNT, 1));
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return simpleCodec(SeasoningBottle::new);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, COUNT);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPES[state.getValue(COUNT) - 1];
        }

        @Override
        public InteractionResult useItemOn(
                ItemStack stack,
                BlockState state,
                Level level,
                BlockPos pos,
                Player player,
                InteractionHand hand,
                BlockHitResult hit) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            boolean bottle = PreviewContent.MOD_ID.equals(id.getNamespace())
                    && (id.getPath().equals("empty_seasoning_bottle")
                        || id.getPath().equals("pending_seasoning")
                        || id.getPath().equals("special_seasoning"));
            if (bottle && state.getValue(COUNT) < 4) {
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(pos, state.setValue(COUNT, state.getValue(COUNT) + 1));
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        @Override
        protected InteractionResult useWithoutItem(
                BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            int count = state.getValue(COUNT);
            if (!level.isClientSide()) {
                Item bottle = PreviewContent.item("empty_seasoning_bottle");
                if (bottle != null) player.getInventory().placeItemBackInInventory(new ItemStack(bottle));
                if (count <= 1) level.removeBlock(pos, false);
                else level.setBlockAndUpdate(pos, state.setValue(COUNT, count - 1));
            }
            return InteractionResult.SUCCESS;
        }
    }

    static final class SaplingShape extends Block {
        private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);

        SaplingShape(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return simpleCodec(SaplingShape::new);
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }
    }
}
