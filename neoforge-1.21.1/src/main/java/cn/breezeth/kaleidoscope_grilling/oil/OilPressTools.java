package cn.breezeth.kaleidoscope_grilling.oil;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;

public final class OilPressTools {
  public static final TagKey<Item> PRESS_STONES =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, "press_stones"));

  public static int progress(ItemStack stack) {
    if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AnvilBlock)
      return 4;
    return stack.is(PRESS_STONES) ? 1 : 0;
  }

  private OilPressTools() {}
}
