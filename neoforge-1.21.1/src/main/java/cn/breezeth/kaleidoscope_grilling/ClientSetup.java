package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private static final ResourceLocation SKEWER_STATE = ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "skewer_state");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            register(ModItems.UNFINISHED_SKEWER.get());
            ModItems.RAW_SKEWERS.forEach(item -> register(item.get()));
            ModItems.FIXED_SKEWERS.forEach(item -> register(item.get()));
        });
    }

    private static void register(Item item) {
        ItemProperties.register(item, SKEWER_STATE,
                (stack, level, entity, seed) -> SkeweringHandler.modelState(stack) / 64.0F);
    }

    private ClientSetup() {}
}
