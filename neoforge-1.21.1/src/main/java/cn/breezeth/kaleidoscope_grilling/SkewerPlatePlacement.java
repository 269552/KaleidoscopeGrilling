package cn.breezeth.kaleidoscope_grilling;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class SkewerPlatePlacement {
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (!event.getEntity().isShiftKeyDown()) return;
    ItemStack mainHand = event.getEntity().getMainHandItem();
    if (mainHand.is(ModItems.SKEWER_RECIPE_BOOK.get()) || mainHand.getItem() instanceof RecipeItem)
      return;
    if (event.getHand() == InteractionHand.OFF_HAND) {
      disassembleOffhand(event);
      return;
    }
    ItemStack held = mainHand;
    if (held.is(ModItems.SKEWER_PLATE.get())) return;
    if (SkewerPlateItem.isSkewer(held)) {
      InteractionResult result =
          place(
              event.getLevel(),
              event.getPos(),
              event.getFace(),
              event.getEntity(),
              held,
              List.of(held.copyWithCount(1)));
      if (result.consumesAction()) {
        event.setCanceled(true);
        event.setCancellationResult(result);
      }
      return;
    }
    ItemStack offhand = event.getEntity().getOffhandItem();
    if (!SkeweringHandler.canDisassemble(offhand)) return;
    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);
    if (!event.getLevel().isClientSide
        && SkeweringHandler.disassemble(
            offhand,
            event.getEntity(),
            InteractionHand.OFF_HAND,
            event.getLevel().registryAccess()))
      event
          .getLevel()
          .playSound(
              null,
              event.getPos(),
              ModSounds.SKEWER_DISASSEMBLE.get(),
              SoundSource.PLAYERS,
              0.8F,
              1.0F);
  }

  private static void disassembleOffhand(PlayerInteractEvent.RightClickBlock event) {
    ItemStack offhand = event.getEntity().getOffhandItem();
    if (!SkeweringHandler.canDisassemble(offhand)) return;
    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);
    if (!event.getLevel().isClientSide
        && SkeweringHandler.disassemble(
            offhand,
            event.getEntity(),
            InteractionHand.OFF_HAND,
            event.getLevel().registryAccess()))
      event
          .getLevel()
          .playSound(
              null,
              event.getPos(),
              ModSounds.SKEWER_DISASSEMBLE.get(),
              SoundSource.PLAYERS,
              0.8F,
              1.0F);
  }

  static InteractionResult placePacked(UseOnContext context, List<ItemStack> skewers) {
    if (skewers.isEmpty()) return InteractionResult.PASS;
    return place(
        context.getLevel(),
        context.getClickedPos(),
        context.getClickedFace(),
        context.getPlayer(),
        context.getItemInHand(),
        skewers);
  }

  private static InteractionResult place(
      Level level,
      BlockPos clickedPos,
      Direction face,
      net.minecraft.world.entity.player.Player player,
      ItemStack source,
      List<ItemStack> skewers) {
    if (player == null || face != Direction.UP) return InteractionResult.PASS;
    BlockState support = level.getBlockState(clickedPos);
    if (!Block.isShapeFullBlock(support.getCollisionShape(level, clickedPos)))
      return InteractionResult.PASS;
    BlockPos placePos = clickedPos.above();
    if (!level.getBlockState(placePos).canBeReplaced()) return InteractionResult.PASS;
    if (!level.isClientSide) {
      BlockState state =
          ModBlocks.SKEWER_PLATE
              .get()
              .defaultBlockState()
              .setValue(SkewerPlateBlock.FACING, player.getDirection());
      level.setBlock(placePos, state, 3);
      if (level.getBlockEntity(placePos) instanceof SkewerPlateBlockEntity plate)
        plate.setSkewers(skewers);
      if (!player.getAbilities().instabuild) source.shrink(1);
      level.playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }

  private SkewerPlatePlacement() {}
}
