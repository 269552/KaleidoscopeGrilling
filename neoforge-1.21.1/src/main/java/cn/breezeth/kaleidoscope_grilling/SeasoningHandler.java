package cn.breezeth.kaleidoscope_grilling;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class SeasoningHandler {
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (event.getHand() != InteractionHand.MAIN_HAND
        || !event.getItemStack().is(ModItems.SPECIAL_SEASONING.get())) return;
    if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof SeasonedPotAccess seasoned))
      return;
    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);
    if (!HotFoodConfig.ENABLE_COOKERY_HEAT_AND_SEASONING.get()) {
      if (!event.getLevel().isClientSide)
        event.getEntity()
            .displayClientMessage(
                Component.translatable("message.kaleidoscope_grilling.cookery_integration_disabled"),
                true);
      return;
    }
    if (!event.getLevel().isClientSide) {
      SeasoningUse.apply(event.getEntity(), event.getHand(), event.getItemStack(), seasoned);
    }
  }

  private SeasoningHandler() {}
}
