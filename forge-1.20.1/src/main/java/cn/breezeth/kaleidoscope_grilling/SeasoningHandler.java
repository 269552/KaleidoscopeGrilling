package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class SeasoningHandler {
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (event.getHand() != InteractionHand.MAIN_HAND
        || !event.getItemStack().is(ModItems.SPECIAL_SEASONING.get())) return;
    if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof SeasonedPotAccess seasoned)) {
      if (!event.getLevel().isClientSide) {
        event
            .getEntity()
            .sendSystemMessage(
                Component.literal("[KG] 撒料失败：目标不是可撒料的炒锅或炖锅。").withStyle(ChatFormatting.RED));
      }
      return;
    }
    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);
    if (!event.getLevel().isClientSide) {
      SeasoningUse.apply(event.getEntity(), event.getHand(), event.getItemStack(), seasoned);
    }
  }

  private SeasoningHandler() {}
}
