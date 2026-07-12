package cn.breezeth.kaleidoscope_grilling;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class GrillBlock extends BaseEntityBlock {
    public static final MapCodec<GrillBlock> CODEC = simpleCodec(GrillBlock::new);
    public GrillBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GrillBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.GRILL.get(), GrillBlockEntity::tick);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && OilPotCompat.isOilPot(stack)) {
            int needed = grill.getPhase() == 0 ? grill.getContainerSize() - (int) java.util.stream.IntStream.range(0, grill.getContainerSize()).filter(i -> grill.getItem(i).isEmpty()).count() : 0;
            if (needed > 0 && OilPotCompat.getCount(stack) >= needed) {
                if (!level.isClientSide) { OilPotCompat.consume(stack, grill.brushOil(OilPotCompat.heatDuration(stack))); player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.oiled", needed), true); }
            } else if (!level.isClientSide) player.displayClientMessage(Component.translatable(needed == 0 ? "message.kaleidoscope_grilling.no_brushable_skewers" : "message.kaleidoscope_grilling.not_enough_oil"), true);
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && stack.is(ModItems.SPECIAL_SEASONING.get())) {
            int needed = grill.seasonableCount();
            int remaining = stack.getMaxDamage() - stack.getDamageValue();
            if (needed > 0 && remaining >= needed) {
                if (!level.isClientSide) { grill.season(stack); stack.setDamageValue(stack.getDamageValue() + needed); if (stack.getDamageValue() >= stack.getMaxDamage()) player.setItemInHand(hand, new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get())); }
            } else if (!level.isClientSide) player.displayClientMessage(Component.translatable(needed == 0 ? "message.kaleidoscope_grilling.no_seasonable_skewers" : "message.kaleidoscope_grilling.not_enough_seasoning"), true);
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canAccept(stack)) {
            if (!level.isClientSide) grill.insert(stack, player);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canFlip()) {
            if (!level.isClientSide) grill.flip();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockEntity(pos) instanceof GrillBlockEntity grill && grill.canExtract()) {
            if (!level.isClientSide) grill.extractOne(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
