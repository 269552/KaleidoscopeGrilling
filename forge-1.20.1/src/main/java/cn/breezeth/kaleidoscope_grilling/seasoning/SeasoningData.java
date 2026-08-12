package cn.breezeth.kaleidoscope_grilling.seasoning;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public final class SeasoningData {
  public static final int MAX_USES = 16;
  private static final String KEY = "SeasoningIngredients";
  private static final String VARIANT_KEY = "SeasoningVariant";
  private static final String USES_KEY = "SeasoningUses";

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

  public static int getUses(ItemStack stack) {
    return stack.hasTag()
        ? net.minecraft.util.Mth.clamp(stack.getTag().getInt(USES_KEY), 0, MAX_USES)
        : 0;
  }

  public static void setUses(ItemStack stack, int uses) {
    stack.getOrCreateTag().putInt(USES_KEY, net.minecraft.util.Mth.clamp(uses, 0, MAX_USES));
  }

  private SeasoningData() {}
}
