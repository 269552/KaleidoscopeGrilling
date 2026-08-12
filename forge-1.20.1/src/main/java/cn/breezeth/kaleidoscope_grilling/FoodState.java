package cn.breezeth.kaleidoscope_grilling;


import java.util.List;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FoodState {
  private static final String HOT_UNTIL = "HotUntil";
  private static final String MODEL_VARIANTS = "SkewerModelVariants";
  private static final String CREATOR = "Creator";
  private static final String CREATOR_NAME = "CreatorName";
  private static final String CREATOR_UUID = "CreatorUuid";
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
    return isHot(first, level) == isHot(second, level) && sameForManualMerge(first, second);
  }

  public static int mergeHot(ItemStack target, ItemStack source, Level level) {
    if (!canMergeHot(target, source, level)) return 0;
    int moved = Math.min(target.getMaxStackSize() - target.getCount(), source.getCount());
    if (moved <= 0) return 0;
    // 双方都是热食：buff 时间按数量加权平均；生食/熟食（非热食）直接合并，不做平均
    if (isHot(target, level) && isHot(source, level)) {
      long now = level.getGameTime();
      long targetHeat = hotUntil(target) - now;
      long sourceHeat = hotUntil(source) - now;
      long averaged = (targetHeat * target.getCount() + sourceHeat * moved) / (target.getCount() + moved);
      target.grow(moved);
      source.shrink(moved);
      setHot(target, now + averaged);
    } else {
      target.grow(moved);
      source.shrink(moved);
    }
    return moved;
  }

  private static long hotUntil(ItemStack stack) {
    return stack.getTag().getLong(HOT_UNTIL);
  }

  private static boolean sameForManualMerge(ItemStack first, ItemStack second) {
    ItemStack firstCopy = first.copy();
    ItemStack secondCopy = second.copy();
    removeMergeIgnoredData(firstCopy);
    removeMergeIgnoredData(secondCopy);
    return ItemStack.isSameItemSameTags(firstCopy, secondCopy);
  }

  private static void removeMergeIgnoredData(ItemStack stack) {
    if (!stack.hasTag()) return;
    var tag = stack.getTag();
    tag.remove(HOT_UNTIL);
    tag.remove(MODEL_VARIANTS);
    tag.remove(CREATOR);
    tag.remove(CREATOR_NAME);
    tag.remove(CREATOR_UUID);
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
