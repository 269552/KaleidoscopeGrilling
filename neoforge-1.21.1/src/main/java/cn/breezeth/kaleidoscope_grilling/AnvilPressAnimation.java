package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.world.entity.player.Player;

public final class AnvilPressAnimation {
    public static final byte EVENT_ID = 68;
    public static final int DURATION_TICKS = 9;
    public static final int IMPACT_TICK = 6;

    public static float slam(float progress) {
        float impact = (float) IMPACT_TICK / DURATION_TICKS;
        float t = Math.max(0.0F, Math.min(1.0F, progress / impact));
        return t * t * t;
    }

    public static float recovery(float progress) {
        float impact = (float) IMPACT_TICK / DURATION_TICKS;
        float t = Math.max(0.0F, Math.min(1.0F, (progress - impact) / (1.0F - impact)));
        return t * t * (3.0F - 2.0F * t);
    }

    public static void start(Player player) {
        ((AnvilPressAnimationAccess) player).grilling$startAnvilPress();
        player.level().broadcastEntityEvent(player, EVENT_ID);
    }

    private AnvilPressAnimation() {}
}
