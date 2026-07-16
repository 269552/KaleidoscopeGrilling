package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.List;

public final class FoodState {
    private static final String HOT_UNTIL = "HotUntil";

    public static void setHot(ItemStack s, long until) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        CompoundTag t = d == null ? new CompoundTag() : d.copyTag();
        t.putLong(HOT_UNTIL, until);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    public static boolean isHot(ItemStack s, Level l) {
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d != null && d.copyTag().getLong(HOT_UNTIL) > l.getGameTime();
    }

    public static void applySeasoning(ItemStack s, Level l, LivingEntity e) {
        if (!isHot(s, l)) return;
        List<String> v = SeasoningData.get(s);
        int duration = 3600;
        long hout = GrillingDataManager.seasoningCount(v, "duration");
        if (hout >= 4) duration *= 4;
        else if (hout > 0) duration *= 2;
        long red = GrillingDataManager.seasoningCount(v, "speed"), gun = GrillingDataManager.seasoningCount(v, "strength");
        if (red > 0) e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, red >= 4 ? 1 : 0));
        if (gun > 0) e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, gun >= 4 ? 1 : 0));
        AdvancedSeasoningHandler.apply(e, v, duration);
    }

    private FoodState() {}
}
