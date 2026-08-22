package cn.breezeth.kaleidoscope_grilling.oil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class OilPotCompat {
  public static final int FAT_CAPACITY = 256;
  public static final int FLUID_CAPACITY = 64;

  private static final String OIL_COUNT = "oil_count";
  private static final String OIL_TYPE = "grilling_oil_type";

  public static boolean isOilPot(ItemStack stack) {
    var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
    return id != null && id.toString().equals("kaleidoscope_cookery:oil_pot");
  }

  public static boolean isCookeryFat(ItemStack stack) {
    var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
    return id != null && id.toString().equals("kaleidoscope_cookery:oil");
  }

  public static int getCount(ItemStack stack) {
    return stack.hasTag()
        ? Math.min(capacity(getType(stack)), stack.getTag().getInt(OIL_COUNT))
        : 0;
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
    stack.getOrCreateTag().putInt(OIL_COUNT, remaining);
    if (remaining == 0) stack.getOrCreateTag().remove(OIL_TYPE);
    return true;
  }

  public static String getType(ItemStack stack) {
    return stack.hasTag() ? stack.getTag().getString(OIL_TYPE) : "";
  }

  public static void fill(ItemStack stack, String type, int points) {
    stack.getOrCreateTag().putString(OIL_TYPE, type);
    stack
        .getOrCreateTag()
        .putInt(OIL_COUNT, Math.min(FLUID_CAPACITY, getCount(stack) + points));
  }

  /** Gives generated display stacks a searchable, oil-specific name. */
  public static void nameForDisplay(ItemStack stack) {
    String type = getType(stack);
    if (!type.isEmpty())
      stack.setHoverName(
          Component.translatable("item.kaleidoscope_grilling.oil_pot." + type));
  }

  /** JEI identity: stored amount must not create dozens of duplicate oil-pot entries. */
  public static String jeiSubtype(ItemStack stack) {
    String type = getType(stack);
    return type.isEmpty() ? (getCount(stack) > 0 ? "fat" : "empty") : type;
  }

  public static void setType(ItemStack stack, String type) {
    stack.getOrCreateTag().putString(OIL_TYPE, type);
    if (type != null && !type.isEmpty() && stack.getOrCreateTag().getInt(OIL_COUNT) > FLUID_CAPACITY) {
      stack.getOrCreateTag().putInt(OIL_COUNT, FLUID_CAPACITY);
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
