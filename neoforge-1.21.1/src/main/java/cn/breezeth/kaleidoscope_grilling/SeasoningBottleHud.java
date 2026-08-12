package cn.breezeth.kaleidoscope_grilling;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class SeasoningBottleHud {
  private static final int WIDTH = 164;
  private static final List<String> REQUIRED =
      List.of(
          "kaleidoscope_grilling:green_chili_powder",
          "kaleidoscope_grilling:sichuan_pepper",
          "kaleidoscope_grilling:onion_powder");

  @SubscribeEvent
  public static void render(RenderGuiLayerEvent.Post event) {
    if (!HudControl.isEnabled()) return;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.options.hideGui
        || minecraft.level == null
        || !(minecraft.hitResult instanceof BlockHitResult hit)
        || !(minecraft.level.getBlockEntity(hit.getBlockPos())
            instanceof SeasoningBottleBlockEntity bottle)) return;
    draw(event.getGuiGraphics(), bottle.ingredients());
  }

  private static void draw(GuiGraphics graphics, List<String> values) {
    Minecraft minecraft = Minecraft.getInstance();
    Map<String, Integer> counts = new LinkedHashMap<>();
    REQUIRED.forEach(id -> counts.put(id, 0));
    values.forEach(id -> counts.merge(id, 1, Integer::sum));
    List<Component> effects = SeasoningEffects.describe(values);
    int height = 49 + counts.size() * 18 + Math.max(1, effects.size()) * 10 + 10;
    int x = OilPotHud.panelX(minecraft, WIDTH);
    int y = Math.max(12, minecraft.getWindow().getGuiScaledHeight() / 2 - height / 2);
    OilPotHud.panel(graphics, x, y, WIDTH, height, 0xFFE0A83B);

    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.seasoning.title"),
        x + 12,
        y + 9,
        0xFFF3E9D2,
        false);
    graphics.renderItem(
        new ItemStack(ModItems.EMPTY_SEASONING_BOTTLE.get()), x + WIDTH - 27, y + 6);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.seasoning.capacity",
            values.size(),
            SeasoningBottleBlockEntity.CAPACITY,
            SeasoningBottleBlockEntity.CAPACITY - values.size()),
        x + 12,
        y + 20,
        0xFFAAB7B8,
        false);
    graphics.fill(x + 12, y + 34, x + WIDTH - 12, y + 38, 0xFF303B3D);
    graphics.fill(
        x + 12,
        y + 34,
        x + 12 + (WIDTH - 24) * values.size() / SeasoningBottleBlockEntity.CAPACITY,
        y + 38,
        0xFFE0A83B);

    int rowY = y + 44;
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.getKey()));
      graphics.renderItem(new ItemStack(item), x + 12, rowY - 3);
      graphics.drawString(minecraft.font, item.getDescription(), x + 34, rowY, 0xFFE5E8E8, false);
      Component value =
          entry.getValue() == 0
              ? Component.translatable("hud.kaleidoscope_grilling.seasoning.missing")
              : Component.literal("x" + entry.getValue());
      graphics.drawString(
          minecraft.font,
          value,
          x + WIDTH - minecraft.font.width(value) - 12,
          rowY,
          entry.getValue() == 0 ? 0xFFE68B73 : 0xFFAAB7B8,
          false);
      rowY += 18;
    }
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.seasoning.effects"),
        x + 12,
        rowY + 1,
        0xFFE0A83B,
        false);
    rowY += 12;
    if (effects.isEmpty())
      effects = List.of(Component.translatable("hud.kaleidoscope_grilling.seasoning.no_effect"));
    for (Component effect : effects) {
      graphics.drawString(minecraft.font, effect, x + 12, rowY, 0xFFB8D8CD, false);
      rowY += 10;
    }
  }

  private SeasoningBottleHud() {}
}
