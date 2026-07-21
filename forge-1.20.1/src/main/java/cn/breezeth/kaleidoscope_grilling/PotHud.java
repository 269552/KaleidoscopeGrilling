package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class PotHud {
  private static final int WIDTH = 190;
  private static final int HEIGHT = 106;

  @SubscribeEvent
  public static void render(RenderGuiOverlayEvent.Post event) {
    if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;
    if (!HudControl.isEnabled()) return;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level == null
        || minecraft.options.hideGui
        || !(minecraft.hitResult instanceof BlockHitResult hit)
        || !(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof PotHudAccess pot)
        || !(pot instanceof SeasonedPotAccess seasoning)
        || !(pot instanceof PotOilAccess oil)) return;
    var state = minecraft.level.getBlockState(hit.getBlockPos());
    boolean hasOil =
        state.getProperties().stream()
            .filter(
                property ->
                    property instanceof BooleanProperty && property.getName().equals("has_oil"))
            .map(property -> state.getValue((BooleanProperty) property))
            .findFirst()
            .orElse(false);
    draw(event.getGuiGraphics(), pot, seasoning, oil, hasOil);
  }

  private static void draw(
      GuiGraphics graphics,
      PotHudAccess pot,
      SeasonedPotAccess seasoning,
      PotOilAccess oil,
      boolean hasOil) {
    Minecraft minecraft = Minecraft.getInstance();
    int x = OilPotHud.panelX(minecraft, WIDTH);
    int y = Math.max(10, minecraft.getWindow().getGuiScaledHeight() / 2 - HEIGHT / 2);
    OilPotHud.panel(graphics, x, y, WIDTH, HEIGHT, 0xFFE0A83B);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.pot.title"),
        x + 12,
        y + 9,
        0xFFF3E9D2,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.pot.status", status(pot, seasoning, hasOil, minecraft)),
        x + 12,
        y + 21,
        0xFFB8D8CD,
        false);
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.pot.ingredients"),
        x + 12,
        y + 36,
        0xFFE0A83B,
        false);
    drawIngredients(graphics, pot.grilling$getInputs(), x + 12, y + 47);
    String type = oil.grilling$getOilType();
    Component oilName =
        hasOil
            ? Component.translatable(
                "hud.kaleidoscope_grilling.pot.oil." + (type.isEmpty() ? "fat" : type))
            : Component.translatable("hud.kaleidoscope_grilling.pot.oil.none");
    graphics.drawString(
        minecraft.font,
        Component.translatable("hud.kaleidoscope_grilling.pot.oil", oilName),
        x + 12,
        y + 76,
        0xFFAAB7B8,
        false);
    boolean seasoned = !seasoning.grilling$getSeasoning().isEmpty();
    graphics.drawString(
        minecraft.font,
        Component.translatable(
            "hud.kaleidoscope_grilling.pot.seasoning." + (seasoned ? "added" : "none")),
        x + 12,
        y + 90,
        seasoned ? 0xFFE0A83B : 0xFF8D9697,
        false);
  }

  private static void drawIngredients(GuiGraphics graphics, List<ItemStack> inputs, int x, int y) {
    int shown = 0;
    for (ItemStack stack : inputs) {
      if (stack.isEmpty()) continue;
      graphics.renderItem(stack, x + shown * 19, y);
      shown++;
    }
    if (shown == 0)
      graphics.drawString(
          Minecraft.getInstance().font,
          Component.translatable("hud.kaleidoscope_grilling.pot.ingredients.empty"),
          x,
          y + 4,
          0xFF8D9697,
          false);
  }

  private static Component status(
      PotHudAccess pot, SeasonedPotAccess seasoning, boolean hasOil, Minecraft minecraft) {
    if (!pot.grilling$hasHeatSource(minecraft.level))
      return Component.translatable("hud.kaleidoscope_grilling.pot.state.need_heat");
    if (!hasOil) return Component.translatable("hud.kaleidoscope_grilling.pot.state.need_oil");
    if (pot.grilling$getStatus() >= 2)
      return Component.translatable(
          "hud.kaleidoscope_grilling.pot.state."
              + (seasoning.grilling$getSeasoning().isEmpty() ? "take_or_season" : "take"));
    if (pot.grilling$getStatus() == 1 || !seasoning.grilling$getSeasoning().isEmpty())
      return Component.translatable("hud.kaleidoscope_grilling.pot.state.stir");
    return Component.translatable("hud.kaleidoscope_grilling.pot.state.stir_or_season");
  }

  private PotHud() {}
}
