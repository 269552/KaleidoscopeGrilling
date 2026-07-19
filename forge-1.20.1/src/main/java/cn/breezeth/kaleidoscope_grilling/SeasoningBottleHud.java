package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class SeasoningBottleHud {
    private static final int WIDTH = 164;
    private static final List<String> REQUIRED = List.of(
            "kaleidoscope_grilling:green_chili_powder",
            "kaleidoscope_grilling:sichuan_pepper",
            "kaleidoscope_grilling:onion_powder"
    );

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
        if (!HudControl.isEnabled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult hit)) return;
        BlockPos pos = hit.getBlockPos();
        if (!(minecraft.level.getBlockEntity(pos) instanceof SeasoningBottleBlockEntity bottle)) return;
        List<String> values = bottle.ingredients();
        draw(event.getGuiGraphics(), values);
    }

    private static void draw(GuiGraphics graphics, List<String> values) {
        Minecraft minecraft = Minecraft.getInstance();
        Map<String, Integer> counts = new LinkedHashMap<>();
        REQUIRED.forEach(id -> counts.put(id, 0));
        values.forEach(id -> counts.merge(id, 1, Integer::sum));
        int effectLines = Math.max(1, SeasoningEffects.describe(values).size());
        int height = 49 + counts.size() * 18 + effectLines * 10 + 10;
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 24;
        int y = Math.max(12, minecraft.getWindow().getGuiScaledHeight() / 2 - height / 2);
        if (x + WIDTH > minecraft.getWindow().getGuiScaledWidth() - 8) x = minecraft.getWindow().getGuiScaledWidth() - WIDTH - 8;

        graphics.fill(x + 3, y + 3, x + WIDTH + 3, y + height + 3, 0x55000000);
        graphics.fill(x, y, x + WIDTH, y + height, 0xE6121718);
        graphics.fill(x, y, x + 3, y + height, 0xFFE0A83B);
        graphics.fill(x + 3, y + 29, x + WIDTH, y + 30, 0x554F6265);
        graphics.drawString(minecraft.font, Component.translatable("hud.kaleidoscope_grilling.seasoning.title"), x + 12, y + 9, 0xFFF3E9D2, false);
        graphics.renderItem(new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get()),x+WIDTH-27,y+6);
        graphics.drawString(minecraft.font, Component.translatable("hud.kaleidoscope_grilling.seasoning.capacity", values.size(), SeasoningBottleBlockEntity.CAPACITY, SeasoningBottleBlockEntity.CAPACITY - values.size()), x + 12, y + 20, 0xFFAAB7B8, false);
        int barX = x + 12;
        int barY = y + 34;
        graphics.fill(barX, barY, x + WIDTH - 12, barY + 4, 0xFF303B3D);
        graphics.fill(barX, barY, barX + (WIDTH - 24) * values.size() / SeasoningBottleBlockEntity.CAPACITY, barY + 4, 0xFFE0A83B);

        int rowY = y + 44;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getKey()));
            if (item == null) continue;
            ItemStack stack = new ItemStack(item);
            graphics.renderItem(stack, x + 12, rowY - 3);
            graphics.drawString(minecraft.font, item.getDescription(), x + 34, rowY, 0xFFE5E8E8, false);
            if (entry.getValue() == 0) {
                graphics.drawString(minecraft.font, Component.translatable("hud.kaleidoscope_grilling.seasoning.missing"), x + WIDTH - 35, rowY, 0xFFE68B73, false);
            } else {
                graphics.drawString(minecraft.font, "x" + entry.getValue(), x + WIDTH - 25, rowY, 0xFFAAB7B8, false);
            }
            rowY += 18;
        }
        graphics.drawString(minecraft.font, Component.translatable("hud.kaleidoscope_grilling.seasoning.effects"), x + 12, rowY + 1, 0xFFE0A83B, false);
        rowY += 12;
        List<Component> effects = SeasoningEffects.describe(values);
        if (effects.isEmpty()) effects = List.of(Component.translatable("hud.kaleidoscope_grilling.seasoning.no_effect"));
        for (Component effect : effects) {
            graphics.drawString(minecraft.font, effect, x + 12, rowY, 0xFFB8D8CD, false);
            rowY += 10;
        }
    }

    private SeasoningBottleHud() {}
}
