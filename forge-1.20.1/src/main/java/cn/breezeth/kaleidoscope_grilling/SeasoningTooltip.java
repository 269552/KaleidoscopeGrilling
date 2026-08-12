package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public final class SeasoningTooltip {
  private static final int CAPACITY = SeasoningBottleBlockEntity.CAPACITY;
  private static final List<String> REQUIRED =
      List.of(
          "kaleidoscope_grilling:green_chili_powder",
          "kaleidoscope_grilling:sichuan_pepper",
          "kaleidoscope_grilling:onion_powder");

  public static void append(ItemStack stack, List<Component> tooltip) {
    if (stack.is(ModItems.SPECIAL_SEASONING.get())) appendFinished(stack, tooltip);
    else appendUnfinished(stack, tooltip);
  }

  public static void appendUnfinished(ItemStack stack, List<Component> tooltip) {
    List<String> ingredients = SeasoningData.get(stack);
    tooltip.add(
        Component.translatable(
                "tooltip.kaleidoscope_grilling.seasoning.capacity", ingredients.size(), CAPACITY)
            .withStyle(ChatFormatting.GRAY));
    if (ingredients.isEmpty()) {
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.seasoning.place_and_shake")
              .withStyle(ChatFormatting.DARK_GRAY));
      return;
    }
    List<String> missing = REQUIRED.stream().filter(id -> !ingredients.contains(id)).toList();
    if (missing.isEmpty()) {
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.seasoning.ready_to_shake")
              .withStyle(ChatFormatting.YELLOW));
    } else {
      tooltip.add(
          Component.translatable(
                  "tooltip.kaleidoscope_grilling.seasoning.missing_required", joinedNames(missing))
              .withStyle(ChatFormatting.RED));
    }
    appendIngredients(ingredients, tooltip);
  }

  public static void appendFinished(ItemStack stack, List<Component> tooltip) {
    int remaining = Math.max(0, SeasoningData.MAX_USES - SeasoningData.getUses(stack));
    tooltip.add(
        Component.translatable(
                "tooltip.kaleidoscope_grilling.seasoning.uses_full",
                remaining,
                SeasoningData.MAX_USES)
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.seasoning.shift_details")
            .withStyle(ChatFormatting.GOLD));
    if (!Screen.hasShiftDown()) return;
    List<String> ingredients = SeasoningData.get(stack);
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.seasoning.provided_effects")
            .withStyle(ChatFormatting.AQUA));
    List<Component> effects = SeasoningEffects.describe(ingredients);
    if (effects.isEmpty()) {
      tooltip.add(
          Component.literal("- ")
              .append(Component.translatable("hud.kaleidoscope_grilling.seasoning.no_effect"))
              .withStyle(ChatFormatting.AQUA));
    } else {
      effects.forEach(
          effect ->
              tooltip.add(Component.literal("- ").append(effect).withStyle(ChatFormatting.AQUA)));
    }
    appendIngredients(ingredients, tooltip);
  }

  private static void appendIngredients(List<String> ingredients, List<Component> tooltip) {
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.seasoning.added_ingredients")
            .withStyle(ChatFormatting.DARK_GRAY));
    for (String id : ingredients) {
      tooltip.add(
          Component.literal("- ")
              .append(item(id).getDescription())
              .withStyle(ChatFormatting.DARK_GRAY));
    }
  }

  private static Component joinedNames(List<String> ids) {
    MutableComponent result = Component.empty();
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0) result.append(Component.literal("、"));
      result.append(item(ids.get(i)).getDescription());
    }
    return result;
  }

  private static Item item(String id) {
    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
    return item == null ? Items.AIR : item;
  }

  private SeasoningTooltip() {}
}
