package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Preserves the source model when a skewer becomes a failed grilling result. */
public final class FailedSkewerData {
  private static final String SOURCE_TAG = "FailedSkewerSource";
  private static final String CUSTOM_SOURCE = "custom";

  private FailedSkewerData() {}

  public static ItemStack create(ItemStack input, Item failedResult) {
    ItemStack output = new ItemStack(failedResult);
    CompoundTag tag = input.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putString(
        SOURCE_TAG,
        input.is(ModItems.SECRET_SKEWER.get())
            ? CUSTOM_SOURCE
            : BuiltInRegistries.ITEM.getKey(input.getItem()).toString());
    output.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return output;
  }

  public static void setCreativeSource(ItemStack stack, Item source) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putString(SOURCE_TAG, BuiltInRegistries.ITEM.getKey(source).toString());
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static float modelValue(ItemStack stack) {
    String source = source(stack);
    for (int index = 0; index < ModItems.RAW_SKEWERS.size(); index++) {
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(ModItems.RAW_SKEWERS.get(index).get());
      if (id.toString().equals(source)) return (index + 1) / 32.0F;
    }
    return 0.0F;
  }

  private static String source(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? CUSTOM_SOURCE : data.copyTag().getString(SOURCE_TAG);
  }
}
