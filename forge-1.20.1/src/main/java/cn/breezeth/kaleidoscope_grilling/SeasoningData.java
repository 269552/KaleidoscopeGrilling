package cn.breezeth.kaleidoscope_grilling;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public final class SeasoningData {
  private static final String KEY = "SeasoningIngredients";
  private static final String VARIANT_KEY = "SeasoningVariant";

  public static List<String> get(ItemStack stack) {
    List<String> out = new ArrayList<>();
    if (!stack.hasTag()) return out;
    ListTag list = stack.getTag().getList(KEY, 8);
    for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
    return out;
  }

  public static void set(ItemStack stack, List<String> values) {
    ListTag list = new ListTag();
    values.forEach(v -> list.add(StringTag.valueOf(v)));
    stack.getOrCreateTag().put(KEY, list);
  }

  public static int getVariant(ItemStack stack) {
    return stack.hasTag()
        ? net.minecraft.util.Mth.clamp(stack.getTag().getInt(VARIANT_KEY), 0, 7)
        : 0;
  }

  public static void setVariant(ItemStack stack, int variant) {
    stack.getOrCreateTag().putInt(VARIANT_KEY, net.minecraft.util.Mth.clamp(variant, 0, 7));
  }

  public static void setRandomVariant(ItemStack stack) {
    setVariant(stack, net.minecraft.util.RandomSource.create().nextInt(8));
  }

  private SeasoningData() {}
}
