package cn.breezeth.kaleidoscope_grilling.rack;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedRackBlockItem extends BlockItem {
  public AdvancedRackBlockItem(Block block, Properties properties) {
    super(block, properties);
  }

  @Override
  protected boolean updateCustomBlockEntityTag(
      BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
    boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
    if (data != null && level.getBlockEntity(pos) instanceof AdvancedRackBlockEntity rack) {
      changed |= data.loadInto(rack, level.registryAccess());
      rack.refreshAfterPlacement();
    }
    return changed;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
    if (data == null) return;
    CompoundTag tag = data.copyTag();
    NonNullList<ItemStack> items =
        NonNullList.withSize(AdvancedRackBlockEntity.COMPARTMENT_COUNT, ItemStack.EMPTY);
    NonNullList<ItemStack> filters =
        NonNullList.withSize(AdvancedRackBlockEntity.COMPARTMENT_COUNT, ItemStack.EMPTY);
    ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
    if (contents != null) contents.copyInto(items);
    else ContainerHelper.loadAllItems(tag, items, context.registries());
    if (tag.contains("Filters"))
      ContainerHelper.loadAllItems(tag.getCompound("Filters"), filters, context.registries());
    appendStoredContents(tooltip, items, filters);
  }

  private static void appendStoredContents(
      List<Component> tooltip, List<ItemStack> items, List<ItemStack> filters) {
    boolean hasStoredSlot = false;
    for (int slot = 0; slot < AdvancedRackBlockEntity.COMPARTMENT_COUNT; slot++) {
      if (!items.get(slot).isEmpty() || !filters.get(slot).isEmpty()) {
        hasStoredSlot = true;
        break;
      }
    }
    if (!hasStoredSlot) return;
    tooltip.add(
        Component.translatable("tooltip.kaleidoscope_grilling.advanced_rack.saved_contents")
            .withStyle(ChatFormatting.GRAY));
    for (int slot = 0; slot < AdvancedRackBlockEntity.COMPARTMENT_COUNT; slot++) {
      ItemStack stored = items.get(slot);
      ItemStack display = stored.isEmpty() ? filters.get(slot) : stored;
      if (display.isEmpty()) continue;
      tooltip.add(
          Component.literal("- ")
              .append(display.getHoverName())
              .append(Component.literal(" x " + stored.getCount()))
              .withStyle(ChatFormatting.DARK_GRAY));
    }
  }
}
