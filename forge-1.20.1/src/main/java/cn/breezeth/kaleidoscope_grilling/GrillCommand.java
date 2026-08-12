package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class GrillCommand {
  public static void register(RegisterCommandsEvent event) {
    event
        .getDispatcher()
        .register(
            Commands.literal("kg")
                .then(
                    Commands.literal("grill")
                        .then(
                            Commands.literal("unlock")
                                .executes(
                                    context ->
                                        unlock(context.getSource().getPlayerOrException())))
                        .then(
                            Commands.literal("unlockall")
                                .requires(source -> source.hasPermission(2))
                                .executes(
                                    context -> {
                                      int count =
                                          GrillAutomationApi.forceUnlockAll(
                                              context.getSource().getServer());
                                      context
                                          .getSource()
                                          .sendSuccess(
                                              () ->
                                                  Component.translatable(
                                                      "command.kaleidoscope_grilling.grill.unlock_all",
                                                      count),
                                              true);
                                      return count;
                                    }))));
  }

  private static int unlock(ServerPlayer player) {
    HitResult hit = player.pick(6.0D, 0.0F, false);
    if (!(hit instanceof BlockHitResult blockHit)
        || !(player.level().getBlockEntity(blockHit.getBlockPos())
            instanceof GrillBlockEntity)) {
      player.displayClientMessage(
          Component.translatable("command.kaleidoscope_grilling.grill.not_found"), true);
      return 0;
    }
    boolean unlocked = GrillAutomationApi.forceUnlock(player.level(), blockHit.getBlockPos());
    player.displayClientMessage(
        Component.translatable(
            unlocked
                ? "command.kaleidoscope_grilling.grill.unlocked"
                : "command.kaleidoscope_grilling.grill.not_locked"),
        true);
    return unlocked ? 1 : 0;
  }

  private GrillCommand() {}
}
