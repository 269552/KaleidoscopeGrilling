package cn.breezeth.kaleidoscope_grilling.event;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class WeddingCandyHandler {
  private static final ZoneId EVENT_TIME_ZONE = ZoneId.of("Asia/Shanghai");
  private static final String PREFIX = KaleidoscopeGrilling.MOD_ID + ":wedding_candy_";
  private static final String TRACKING_DATE = PREFIX + "tracking_date";
  private static final String PLAY_SECONDS = PREFIX + "play_seconds";
  private static final String CLAIMED_DATE = PREFIX + "claimed_date";

  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END
        || !(event.player instanceof ServerPlayer player)
        || player.tickCount % 20 != 0) return;

    LocalDate date = LocalDate.now(EVENT_TIME_ZONE);
    if (!isEventDate(date)) return;

    CompoundTag data = player.getPersistentData();
    String today = date.toString();
    if (today.equals(data.getString(CLAIMED_DATE))) return;
    if (!today.equals(data.getString(TRACKING_DATE))) {
      data.putString(TRACKING_DATE, today);
      data.putInt(PLAY_SECONDS, 0);
    }

    int seconds = data.getInt(PLAY_SECONDS) + 1;
    data.putInt(PLAY_SECONDS, seconds);
    int amount = date.getDayOfMonth();
    if (seconds < amount * 60) return;

    data.putString(CLAIMED_DATE, today);
    ItemStack reward = new ItemStack(ModItems.WEDDING_CANDY.get(), amount);
    if (!player.getInventory().add(reward) && !reward.isEmpty()) player.drop(reward, false);
    player.displayClientMessage(
        Component.translatable("message.kaleidoscope_grilling.wedding_candy.received", amount),
        false);
    ModAdvancements.weddingCandy(player);
  }

  public static void onPlayerClone(PlayerEvent.Clone event) {
    CompoundTag source = event.getOriginal().getPersistentData();
    CompoundTag target = event.getEntity().getPersistentData();
    copyString(source, target, TRACKING_DATE);
    copyString(source, target, CLAIMED_DATE);
    if (source.contains(PLAY_SECONDS)) target.putInt(PLAY_SECONDS, source.getInt(PLAY_SECONDS));
  }

  private static boolean isEventDate(LocalDate date) {
    return date.getYear() == 2026
        && date.getMonth() == Month.SEPTEMBER
        && date.getDayOfMonth() >= 1
        && date.getDayOfMonth() <= 12;
  }

  private static void copyString(CompoundTag source, CompoundTag target, String key) {
    if (source.contains(key)) target.putString(key, source.getString(key));
  }

  private WeddingCandyHandler() {}
}
