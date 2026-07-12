package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;

public final class SeasoningData {
    private static final String KEY = "SeasoningIngredients";
    public static List<String> get(ItemStack stack) { List<String> out = new ArrayList<>(); CustomData data = stack.get(DataComponents.CUSTOM_DATA); if (data == null) return out; ListTag list = data.copyTag().getList(KEY, 8); for (int i=0;i<list.size();i++) out.add(list.getString(i)); return out; }
    public static void set(ItemStack stack, List<String> values) { ListTag list = new ListTag(); values.forEach(v -> list.add(StringTag.valueOf(v))); CustomData existing=stack.get(DataComponents.CUSTOM_DATA); CompoundTag tag=existing==null?new CompoundTag():existing.copyTag(); tag.put(KEY, list); stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); }
    private SeasoningData() {}
}
