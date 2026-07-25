package cn.breezeth.kaleidoscope_grilling;

import java.util.List;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FoodState {
  private static final String HOT_UNTIL = "HotUntil";
  private static final long HEAT_BUCKET_TICKS = 5L * 20L;

  public static void setHot(ItemStack s, long until) {
    s.getOrCreateTag().putLong(HOT_UNTIL, bucket(until));
  }

  public static boolean isHot(ItemStack s, Level l) {
    return s.hasTag() && s.getTag().getLong(HOT_UNTIL) > l.getGameTime();
  }

  /** Removes only expired heat data, preserving any other custom item data such as seasoning. */
  public static boolean clearExpired(ItemStack stack, Level level) {
    if (!stack.hasTag()) return false;
    var tag = stack.getTag();
    if (!tag.contains(HOT_UNTIL) || tag.getLong(HOT_UNTIL) > level.getGameTime()) return false;
    tag.remove(HOT_UNTIL);
    if (tag.isEmpty()) stack.setTag(null);
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
    return stack.getTag().getLong(HOT_UNTIL);
  }

  private static boolean sameExceptHeat(ItemStack first, ItemStack second) {
    ItemStack firstCopy = first.copy();
    ItemStack secondCopy = second.copy();
    removeHeat(firstCopy);
    removeHeat(secondCopy);
    return ItemStack.isSameItemSameTags(firstCopy, secondCopy);
  }

  private static void removeHeat(ItemStack stack) {
    if (!stack.hasTag()) return;
    var tag = stack.getTag();
    tag.remove(HOT_UNTIL);
    if (tag.isEmpty()) stack.setTag(null);
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
      e.addEffect(new MobEffectInstance(ModEffects.NUMB.get(), numbDuration));
    }
    AdvancedSeasoningHandler.apply(e, v, duration);
  }

  private FoodState() {}
}
