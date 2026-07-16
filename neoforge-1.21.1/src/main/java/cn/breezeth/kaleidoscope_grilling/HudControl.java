package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class HudControl {
    private static boolean enabled;

    public static boolean isEnabled() {
        return enabled;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kg")
                .then(Commands.literal("hud")
                        .then(Commands.literal("on").executes(context -> set(true)))
                        .then(Commands.literal("off").executes(context -> set(false)))));
    }

    private static int set(boolean value) {
        enabled = value;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.translatable(
                    "command.kaleidoscope_grilling.hud." + (enabled ? "on" : "off")));
        }
        return 1;
    }

    private HudControl() {}
}
