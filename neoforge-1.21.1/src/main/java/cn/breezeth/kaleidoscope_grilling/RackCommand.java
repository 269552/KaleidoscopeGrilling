package cn.breezeth.kaleidoscope_grilling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class RackCommand {
  private static final int RANGE = 8;

  public static void register(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("kgrack")
                .executes(
                    context -> {
                      ServerPlayer player = context.getSource().getPlayerOrException();
                      if (player.isSpectator()) return 0;
                      List<BlockPos> racks = nearby(player);
                      if (racks.isEmpty()) {
                        player.displayClientMessage(
                            Component.translatable("message.kaleidoscope_grilling.rack_not_found"),
                            true);
                        return 0;
                      }
                      if (player.level().getBlockEntity(racks.getFirst())
                          instanceof AdvancedRackBlockEntity rack)
                        player.openMenu(rack.shortcutMenu());
                      return 1;
                    }));
  }

  private static List<BlockPos> nearby(ServerPlayer player) {
    BlockPos origin = player.blockPosition();
    List<BlockPos> racks = new ArrayList<>();
    for (BlockPos pos :
        BlockPos.betweenClosed(
            origin.offset(-RANGE, -RANGE, -RANGE), origin.offset(RANGE, RANGE, RANGE))) {
      if (player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= RANGE * RANGE
          && player.level().hasChunkAt(pos)
          && player.level().getBlockEntity(pos) instanceof AdvancedRackBlockEntity)
        racks.add(pos.immutable());
    }
    racks.sort(
        Comparator.comparingDouble(
            pos -> player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5)));
    return List.copyOf(racks);
  }

  private RackCommand() {}
}
