package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public final class SeasoningData {
    private static final String KEY = "SeasoningIngredients";
    public static List<String> get(ItemStack stack) { List<String> out = new ArrayList<>(); if (!stack.hasTag()) return out; ListTag list = stack.getTag().getList(KEY, 8); for (int i=0;i<list.size();i++) out.add(list.getString(i)); return out; }
    public static void set(ItemStack stack, List<String> values) { ListTag list = new ListTag(); values.forEach(v -> list.add(StringTag.valueOf(v))); stack.getOrCreateTag().put(KEY, list); }
    private SeasoningData() {}
}
