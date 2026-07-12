package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private static final ResourceLocation SKEWER_STATE = new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "skewer_state");

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
