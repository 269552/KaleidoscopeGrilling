package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class OilPotCompat {
    private static final String OIL_COUNT = "oil_count";
    private static final String OIL_TYPE = "grilling_oil_type";
    public static boolean isOilPot(ItemStack stack) {
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.toString().equals("kaleidoscope_cookery:oil_pot");
    }
    public static int getCount(ItemStack stack) { return stack.hasTag() ? Math.min(64, stack.getTag().getInt(OIL_COUNT)) : 0; }
    public static boolean consume(ItemStack stack, int amount) {
        int count = getCount(stack);
        if (amount <= 0 || count < amount) return false;
        stack.getOrCreateTag().putInt(OIL_COUNT, count - amount);
        return true;
    }
    public static String getType(ItemStack stack) { return stack.hasTag() ? stack.getTag().getString(OIL_TYPE) : ""; }
    public static void fill(ItemStack stack,String type,int points){stack.getOrCreateTag().putString(OIL_TYPE,type);stack.getOrCreateTag().putInt(OIL_COUNT,Math.min(64,getCount(stack)+points));}
    public static void setType(ItemStack stack,String type){stack.getOrCreateTag().putString(OIL_TYPE,type);}
    public static int heatDuration(ItemStack stack){return switch(getType(stack)){case "secret_chili"->12000;case "premium_chili"->24000;default->1200;};}
    private OilPotCompat() {}
}
