package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private static final ResourceLocation SKEWER_STATE = new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "skewer_state");
    private static final ResourceLocation OIL_TYPE = new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "oil_type");
    private static final ResourceLocation SEASONING_FILL = new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "seasoning_fill");
    private static final ResourceLocation SEASONING_REMAINING = new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "seasoning_remaining");
    private static final ResourceLocation SEASONING_VARIANT = new ResourceLocation(KaleidoscopeGrilling.MOD_ID, "seasoning_variant");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ADVANCED_RACK.get(), AdvancedRackScreen::new);
            MenuScreens.register(ModMenus.RACK_SELECTION.get(), RackSelectionScreen::new);
            MenuScreens.register(ModMenus.RACK_SHORTCUT.get(), RackShortcutScreen::new);
            BlockEntityRenderers.register(ModBlockEntities.GRILL.get(), GrillRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.SEASONING_BOTTLE.get(), SeasoningBottleRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.BIG_VAT.get(), BigVatRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.ADVANCED_RACK.get(), AdvancedRackRenderer::new);
            BlockEntityRenderers.register(ModBlockEntities.SKEWER_RECIPE.get(), SkewerRecipeBlockRenderer::new);
            register(ModItems.UNFINISHED_SKEWER.get());
            register(ModItems.SECRET_SKEWER.get());
            ModItems.RAW_SKEWERS.forEach(item -> register(item.get()));
            ModItems.FIXED_SKEWERS.forEach(item -> register(item.get()));
            Item oilPot = ForgeRegistries.ITEMS.getValue(new ResourceLocation("kaleidoscope_cookery", "oil_pot"));
            if (oilPot != null) ItemProperties.register(oilPot, OIL_TYPE,
                    (stack, level, entity, seed) -> OilPotCompat.getCount(stack) > 0
                            ? oilTypeModelValue(OilPotCompat.getType(stack)) : 0F);
            Block oilPotBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("kaleidoscope_cookery", "oil_pot"));
            if (oilPotBlock != null) ItemBlockRenderTypes.setRenderLayer(oilPotBlock, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SEASONING_BOTTLE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BIG_VAT.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ADVANCED_RACK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SKEWER_RECIPE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PEPPER_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PEPPER_SAPLING.get(), RenderType.cutout());
            ItemProperties.register(ModItems.EMPTY_SEASONING_BOTTLE.get(), SEASONING_FILL,
                    (stack, level, entity, seed) -> Math.min(8, SeasoningData.get(stack).size()) / 8.0F);
            ItemProperties.register(ModItems.PENDING_SEASONING.get(), SEASONING_FILL,
                    (stack, level, entity, seed) -> Math.min(8, SeasoningData.get(stack).size()) / 8.0F);
            ItemProperties.register(ModItems.SPECIAL_SEASONING.get(), SEASONING_REMAINING,
                    (stack, level, entity, seed) -> Math.min(8, (stack.getMaxDamage() - stack.getDamageValue() + 1) / 2) / 8.0F);
            ItemProperties.register(ModItems.SPECIAL_SEASONING.get(), SEASONING_VARIANT,
                    (stack, level, entity, seed) -> SeasoningData.getVariant(stack) / 7.0F);
        });
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(SkewerColorProvider::color, ModItems.UNFINISHED_SKEWER.get(), ModItems.SECRET_SKEWER.get());
        event.register(SeasoningColorProvider::color, ModItems.EMPTY_SEASONING_BOTTLE.get(), ModItems.PENDING_SEASONING.get());
    }

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> SkewerColorProvider.clearCache());
        event.registerReloadListener((ResourceManagerReloadListener) manager -> SeasoningColorProvider.clearCache());
    }

    private static void register(Item item) {
        ItemProperties.register(item, SKEWER_STATE,
                (stack, level, entity, seed) -> SkeweringHandler.modelState(stack) / 64.0F);
    }

    private static float oilTypeModelValue(String type) {
        return switch (type) {
            case "canola" -> 1F;
            case "secret_chili" -> 2F;
            case "premium_chili" -> 3F;
            default -> 0F;
        };
    }

    private ClientSetup() {}
}
