package cn.breezeth.kaleidoscope_grilling.mixin;

import cn.breezeth.kaleidoscope_grilling.AnvilPressAnimation;
import cn.breezeth.kaleidoscope_grilling.AnvilPressAnimationAccess;
import cn.breezeth.kaleidoscope_grilling.SeasoningAnimation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAnvilPressMixin implements AnvilPressAnimationAccess {
    @Unique private long grilling$anvilPressStart = Long.MIN_VALUE;
    @Unique private long grilling$seasoningStart = Long.MIN_VALUE;

    @Override public void grilling$startAnvilPress() { grilling$anvilPressStart = player().level().getGameTime(); }
    @Override public float grilling$getAnvilPressProgress(float partialTick) {
        return progress(grilling$anvilPressStart, AnvilPressAnimation.DURATION_TICKS, partialTick);
    }
    @Override public void grilling$startSeasoning() { grilling$seasoningStart = player().level().getGameTime(); }
    @Override public float grilling$getSeasoningProgress(float partialTick) {
        return progress(grilling$seasoningStart, SeasoningAnimation.DURATION_TICKS, partialTick);
    }

    @Unique private Player player() { return (Player) (Object) this; }
    @Unique private float progress(long start, int duration, float partialTick) {
        float elapsed = player().level().getGameTime() + partialTick - start;
        return elapsed < 0 || elapsed >= duration ? -1.0F : elapsed / duration;
    }

    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    private void grilling$animationEvent(byte id, CallbackInfo ci) {
        if (id == AnvilPressAnimation.EVENT_ID) grilling$startAnvilPress();
        else if (id == SeasoningAnimation.EVENT_ID) grilling$startSeasoning();
        else return;
        ci.cancel();
    }
}
