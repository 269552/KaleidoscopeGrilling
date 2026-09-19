package cn.breezeth.kaleidoscope_grilling.seasoning;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class SeasoningData {
  public static final int MAX_USES = 16;
  private static final String KEY = "SeasoningIngredients";
  private static final String VARIANT_KEY = "SeasoningVariant";
  private static final String USES_KEY = "SeasoningUses";

  public static List<String> get(ItemStack stack) {
    List<String> out = new ArrayList<>();
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return out;
    ListTag list = data.copyTag().getList(KEY, 8);
    for (int i = 0; i < list.size(); i++) out.add(list.getString(i));
    return out;
  }

  public static void set(ItemStack stack, List<String> values) {
    ListTag list = new ListTag();
    values.forEach(v -> list.add(StringTag.valueOf(v)));
    CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
    CompoundTag tag = existing == null ? new CompoundTag() : existing.copyTag();
    tag.put(KEY, list);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static int getVariant(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null
        ? 0
        : net.minecraft.util.Mth.clamp(data.copyTag().getInt(VARIANT_KEY), 0, 7);
  }

  public static void setVariant(ItemStack stack, int variant) {
    CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
    CompoundTag tag = existing == null ? new CompoundTag() : existing.copyTag();
    tag.putInt(VARIANT_KEY, net.minecraft.util.Mth.clamp(variant, 0, 7));
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static void setRandomVariant(ItemStack stack) {
    setVariant(stack, net.minecraft.util.RandomSource.create().nextInt(8));
  }

  public static int getUses(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null
        ? 0
        : net.minecraft.util.Mth.clamp(data.copyTag().getInt(USES_KEY), 0, MAX_USES);
  }

  public static void setUses(ItemStack stack, int uses) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putInt(USES_KEY, net.minecraft.util.Mth.clamp(uses, 0, MAX_USES));
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  private SeasoningData() {}
}
