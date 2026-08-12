package cn.breezeth.kaleidoscope_grilling.food;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraftforge.event.TickEvent;

/** Periodically removes stale heat timestamps from player-held item stacks. */
public final class HotFoodExpiryHandler {
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END
        || !(event.player instanceof ServerPlayer player)
        || player.tickCount % 20 != 0) return;
    clear(player.getInventory(), player.level());
    clear(player.getEnderChestInventory(), player.level());
  }

  private static void clear(Container inventory, net.minecraft.world.level.Level level) {
    for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
      var stack = inventory.getItem(slot);
      if (FoodState.clearExpired(stack, level)) inventory.setItem(slot, stack);
    }
  }

  private HotFoodExpiryHandler() {}
}
