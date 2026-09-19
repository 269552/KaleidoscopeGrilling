package cn.breezeth.kaleidoscope_grilling.compat.jade;

import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningBottleBlockEntity;
import cn.breezeth.kaleidoscope_grilling.seasoning.SeasoningData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

enum SeasoningBottleProvider implements IBlockComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
    if (!(accessor.getBlockEntity() instanceof SeasoningBottleBlockEntity bottle)
        || bottle.count() != 1) return;
    List<ItemStack> ingredients = new ArrayList<>();
    for (String id : bottle.ingredients()) {
      ResourceLocation itemId = ResourceLocation.tryParse(id);
      if (itemId != null)
        BuiltInRegistries.ITEM
            .getOptional(itemId)
            .ifPresent(item -> ingredients.add(new ItemStack(item)));
    }
    if (!ingredients.isEmpty()) JadeElements.appendItems(tooltip, ingredients);
    if (bottle.isFinished()) {
      int uses =
          Math.max(0, SeasoningData.MAX_USES - SeasoningData.getUses(bottle.top()));
      tooltip.add(Component.translatable("jade.kaleidoscope_grilling.seasoning.uses", uses));
    } else {
      tooltip.add(
          Component.translatable(
              "jade.kaleidoscope_grilling.seasoning.capacity",
              bottle.ingredients().size(),
              SeasoningBottleBlockEntity.CAPACITY));
    }
  }

  @Override
  public ResourceLocation getUid() {
    return GrillingJadePlugin.SEASONING;
  }
}
