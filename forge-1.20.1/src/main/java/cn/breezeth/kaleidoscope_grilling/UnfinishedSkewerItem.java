package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class UnfinishedSkewerItem extends Item {
  public UnfinishedSkewerItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    List<ItemStack> ingredients = SkeweringHandler.readIngredientStacks(stack);
    tooltip.add(
        Component.translatable(
                "tooltip.kaleidoscope_grilling.skewer_progress",
                ingredients.size(),
                SkewerRecipes.expectedSize(ingredients))
            .withStyle(ChatFormatting.YELLOW));
    for (ItemStack ingredient : ingredients)
      tooltip.add(
          Component.literal("- ").append(ingredient.getHoverName()).withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.skewer_finish")
            .withStyle(ChatFormatting.DARK_GRAY));
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.skewer_remove_last")
            .withStyle(ChatFormatting.DARK_GRAY));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide) {
      if (player.isShiftKeyDown()) {
        SkeweringHandler.disassemble(stack, player, hand);
      } else {
        ItemStack result = SkeweringHandler.finishAsSecret(stack, player);
        if (!result.isEmpty()) player.setItemInHand(hand, result);
      }
    }
    return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
  }
}
