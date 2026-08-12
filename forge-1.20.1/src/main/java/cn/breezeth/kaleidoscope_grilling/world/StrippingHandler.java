package cn.breezeth.kaleidoscope_grilling.world;

import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;

public final class StrippingHandler {
  public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
    var state = event.getState();
    if (event.getToolAction() == ToolActions.AXE_STRIP && state.is(ModBlocks.PEPPER_LOG.get())) {
      var axis = state.getValue(RotatedPillarBlock.AXIS);
      event.setFinalState(
          Blocks.STRIPPED_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
    }
  }

  private StrippingHandler() {}
}
