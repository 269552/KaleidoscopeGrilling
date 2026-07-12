package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
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
    public GrillBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GrillBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.GRILL.get(), GrillBlockEntity::tick);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GrillBlockEntity grill)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (OilPotCompat.isOilPot(held)) {
            int needed = grill.getPhase() == 0 ? grill.getContainerSize() - (int) java.util.stream.IntStream.range(0, grill.getContainerSize()).filter(i -> grill.getItem(i).isEmpty()).count() : 0;
            if (needed > 0 && OilPotCompat.getCount(held) >= needed) {
                if (!level.isClientSide) { OilPotCompat.consume(held, grill.brushOil(OilPotCompat.heatDuration(held))); player.displayClientMessage(Component.translatable("message.kaleidoscope_grilling.oiled", needed), true); }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(needed == 0 ? "message.kaleidoscope_grilling.no_brushable_skewers" : "message.kaleidoscope_grilling.not_enough_oil"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(ModItems.SPECIAL_SEASONING.get())) {
            int needed = grill.seasonableCount();
            int remaining = held.getMaxDamage() - held.getDamageValue();
            if (needed > 0 && remaining >= needed) {
                if (!level.isClientSide) { grill.season(held); held.setDamageValue(held.getDamageValue() + needed); if (held.getDamageValue() >= held.getMaxDamage()) player.setItemInHand(hand, new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get())); }
            } else if (!level.isClientSide) player.displayClientMessage(Component.translatable(needed == 0 ? "message.kaleidoscope_grilling.no_seasonable_skewers" : "message.kaleidoscope_grilling.not_enough_seasoning"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty() && grill.canAccept(held)) {
            if (!level.isClientSide) grill.insert(held, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && grill.canFlip()) {
            if (!level.isClientSide) grill.flip();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty() && grill.canExtract()) {
            if (!level.isClientSide) grill.extractOne(player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
