package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class OilPotCompat {
  public static final int FAT_CAPACITY = 256;
  public static final int FLUID_CAPACITY = 64;

  private static final ResourceLocation COUNT_ID =
      ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "oil_pot_oil_count");

  public static boolean isOilPot(ItemStack stack) {
    return BuiltInRegistries.ITEM
        .getKey(stack.getItem())
        .toString()
        .equals("kaleidoscope_cookery:oil_pot");
  }

  public static boolean isCookeryFat(ItemStack stack) {
    return BuiltInRegistries.ITEM
        .getKey(stack.getItem())
        .toString()
        .equals("kaleidoscope_cookery:oil");
  }

  @SuppressWarnings("unchecked")
  private static DataComponentType<Integer> countType() {
    return (DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(COUNT_ID);
  }

  public static int getCount(ItemStack stack) {
    DataComponentType<Integer> type = countType();
    if (type == null) return 0;
    Integer count = stack.get(type);
    return count == null ? 0 : Math.min(capacity(getType(stack)), count);
  }

  public static int capacity(ItemStack stack) {
    return capacity(getType(stack));
  }

  public static int capacity(String type) {
    return type == null || type.isEmpty() ? FAT_CAPACITY : FLUID_CAPACITY;
  }

  public static boolean consume(ItemStack stack, int amount) {
    int count = getCount(stack);
    if (amount <= 0 || count < amount) return false;
    int remaining = count - amount;
    stack.set(countType(), remaining);
    if (remaining == 0) setType(stack, "");
    return true;
  }

  public static String getType(ItemStack stack) {
    CustomData d = stack.get(DataComponents.CUSTOM_DATA);
    return d == null ? "" : d.copyTag().getString("grilling_oil_type");
  }

  public static void fill(ItemStack stack, String type, int points) {
    CustomData d = stack.get(DataComponents.CUSTOM_DATA);
    CompoundTag tag = d == null ? new CompoundTag() : d.copyTag();
    tag.putString("grilling_oil_type", type);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    stack.set(countType(), Math.min(FLUID_CAPACITY, getCount(stack) + points));
  }

  public static void setType(ItemStack stack, String type) {
    Integer storedCount = stack.get(countType());
    CustomData d = stack.get(DataComponents.CUSTOM_DATA);
    CompoundTag tag = d == null ? new CompoundTag() : d.copyTag();
    tag.putString("grilling_oil_type", type);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    if (type != null
        && !type.isEmpty()
        && storedCount != null
        && storedCount > FLUID_CAPACITY) {
      stack.set(countType(), FLUID_CAPACITY);
    }
  }

  public static int heatDuration(ItemStack stack) {
    return switch (getType(stack)) {
      case "secret_chili" -> 12000;
      case "premium_chili" -> 24000;
      default -> 1200;
    };
  }

  private OilPotCompat() {}
}
