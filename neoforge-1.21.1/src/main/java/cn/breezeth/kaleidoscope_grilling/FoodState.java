package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class FoodState {
  private static final String HOT_UNTIL = "HotUntil";
  private static final long HEAT_BUCKET_TICKS = 5L * 20L;

  public static void setHot(ItemStack s, long until) {
    CustomData d = s.get(DataComponents.CUSTOM_DATA);
    CompoundTag t = d == null ? new CompoundTag() : d.copyTag();
    t.putLong(HOT_UNTIL, bucket(until));
    s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
  }

  public static boolean isHot(ItemStack s, Level l) {
    CustomData d = s.get(DataComponents.CUSTOM_DATA);
    return d != null && d.copyTag().getLong(HOT_UNTIL) > l.getGameTime();
  }

  /** Removes only expired heat data, preserving any other custom item data such as seasoning. */
  public static boolean clearExpired(ItemStack stack, Level level) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return false;
    CompoundTag tag = data.copyTag();
    if (!tag.contains(HOT_UNTIL) || tag.getLong(HOT_UNTIL) > level.getGameTime()) return false;
    tag.remove(HOT_UNTIL);
    if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
    else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return true;
  }

  public static boolean canMergeHot(ItemStack first, ItemStack second, Level level) {
    return isHot(first, level) && isHot(second, level) && sameExceptHeat(first, second);
  }

  public static int mergeHot(ItemStack target, ItemStack source, Level level) {
    if (!canMergeHot(target, source, level)) return 0;
    int moved = Math.min(target.getMaxStackSize() - target.getCount(), source.getCount());
    if (moved <= 0) return 0;
    long now = level.getGameTime();
    long targetHeat = hotUntil(target) - now;
    long sourceHeat = hotUntil(source) - now;
    long averaged = (targetHeat * target.getCount() + sourceHeat * moved) / (target.getCount() + moved);
    target.grow(moved);
    source.shrink(moved);
    setHot(target, now + averaged);
    return moved;
  }

  private static long hotUntil(ItemStack stack) {
    return stack.get(DataComponents.CUSTOM_DATA).copyTag().getLong(HOT_UNTIL);
  }

  private static boolean sameExceptHeat(ItemStack first, ItemStack second) {
    ItemStack firstCopy = first.copy();
    ItemStack secondCopy = second.copy();
    removeHeat(firstCopy);
    removeHeat(secondCopy);
    return ItemStack.isSameItemSameComponents(firstCopy, secondCopy);
  }

  private static void removeHeat(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return;
    CompoundTag tag = data.copyTag();
    tag.remove(HOT_UNTIL);
    if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
    else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  private static long bucket(long until) {
    return until - Math.floorMod(until, HEAT_BUCKET_TICKS);
  }

  public static void applySeasoning(ItemStack s, Level l, LivingEntity e) {
    if (!isHot(s, l)) return;
    List<String> v = SeasoningData.get(s);
    int duration = 3600;
    long hout = GrillingDataManager.seasoningCount(v, "duration");
    if (hout >= 4) duration *= 4;
    else if (hout > 0) duration *= 2;
    long red = GrillingDataManager.seasoningCount(v, "speed"),
        gun = GrillingDataManager.seasoningCount(v, "strength");
    if (red > 0)
      e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, red >= 4 ? 1 : 0));
    if (gun > 0)
      e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, gun >= 4 ? 1 : 0));
    long pepper = GrillingDataManager.seasoningCount(v, "numbness");
    if (pepper >= 4) {
      int numbDuration = 45 * 20;
      if (hout >= 4) numbDuration *= 4;
      else if (hout > 0) numbDuration *= 2;
      e.addEffect(new MobEffectInstance(ModEffects.NUMB, numbDuration));
    }
    AdvancedSeasoningHandler.apply(e, v, duration);
  }

  private FoodState() {}
}
