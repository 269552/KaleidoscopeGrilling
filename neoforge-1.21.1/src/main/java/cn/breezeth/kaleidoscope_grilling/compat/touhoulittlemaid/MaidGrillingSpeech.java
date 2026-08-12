package cn.breezeth.kaleidoscope_grilling.compat.touhoulittlemaid;

import cn.breezeth.kaleidoscope_grilling.HotFoodConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.sounds.SoundEvent;

final class MaidGrillingSpeech {
  private static final long MINIMUM_INTERVAL = 20L;
  private static final Map<EntityMaid, BubbleState> STATES = new WeakHashMap<>();
  private static final Map<String, Integer> VARIANT_COUNTS =
      Map.of(
          "bubble.kaleidoscope_grilling.maid_grilling.start", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.missing_skewer", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.missing_flint", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.missing_oil", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.missing_seasoning", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.output_full", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.done", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.missing_chair", 3,
          "bubble.kaleidoscope_grilling.maid_grilling.inventory_full", 3);

  static void say(EntityMaid maid, String translationKey, boolean respectCooldown) {
    if (maid.level().isClientSide) return;
    long now = maid.level().getGameTime();
    BubbleState state = STATES.computeIfAbsent(maid, ignored -> new BubbleState());
    if (respectCooldown && translationKey.equals(state.lastKey) && now < state.nextRepeat) return;
    if (now < state.nextAny) {
      state.pendingKey = translationKey;
      state.pendingRespectCooldown = respectCooldown;
      return;
    }
    display(maid, translationKey, state, now);
  }

  static void tick(EntityMaid maid) {
    BubbleState state = STATES.get(maid);
    if (state == null || state.pendingKey.isEmpty()) return;
    long now = maid.level().getGameTime();
    if (now < state.nextAny) return;
    String key = state.pendingKey;
    boolean respectCooldown = state.pendingRespectCooldown;
    state.pendingKey = "";
    if (respectCooldown && key.equals(state.lastKey) && now < state.nextRepeat) return;
    display(maid, key, state, now);
  }

  private static void display(
      EntityMaid maid, String translationKey, BubbleState state, long now) {
    if (state.bubbleId >= 0L) maid.getChatBubbleManager().removeChatBubble(state.bubbleId);
    state.bubbleId =
        maid.getChatBubbleManager().addTextChatBubble(selectVariant(maid, translationKey, state));
    maid.playSound(voiceFor(translationKey), 0.65F, 1.0F);
    state.lastKey = translationKey;
    state.nextAny = now + MINIMUM_INTERVAL;
    state.nextRepeat =
        now + Math.max(MINIMUM_INTERVAL, HotFoodConfig.MAID_BUBBLE_COOLDOWN_TICKS.get());
  }

  private static String selectVariant(
      EntityMaid maid, String translationKey, BubbleState state) {
    int count = VARIANT_COUNTS.getOrDefault(translationKey, 1);
    if (count <= 1) return translationKey;
    int previous = state.lastVariants.getOrDefault(translationKey, -1);
    int selected = maid.getRandom().nextInt(count);
    if (selected == previous) {
      selected = (selected + 1 + maid.getRandom().nextInt(count - 1)) % count;
    }
    state.lastVariants.put(translationKey, selected);
    return translationKey + "." + (selected + 1);
  }

  private static SoundEvent voiceFor(String key) {
    if (key.endsWith("missing_flint")) return InitSounds.MAID_TORCH.get();
    if (key.endsWith("done") || key.endsWith("missing_oil") || key.endsWith("missing_seasoning"))
      return InitSounds.MAID_FURNACE.get();
    if (key.endsWith("missing_skewer") || key.endsWith("output_full"))
      return InitSounds.MAID_ITEM_GET.get();
    return InitSounds.MAID_IDLE.get();
  }

  private static final class BubbleState {
    private String lastKey = "";
    private long bubbleId = -1L;
    private long nextAny;
    private long nextRepeat;
    private String pendingKey = "";
    private boolean pendingRespectCooldown;
    private final Map<String, Integer> lastVariants = new HashMap<>();
  }

  private MaidGrillingSpeech() {}
}
