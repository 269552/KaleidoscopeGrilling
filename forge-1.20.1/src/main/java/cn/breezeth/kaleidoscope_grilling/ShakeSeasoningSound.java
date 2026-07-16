package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public final class ShakeSeasoningSound extends AbstractTickableSoundInstance {
    private final Player player;

    public ShakeSeasoningSound(Player player) {
        super(ModSounds.SHAKE_SEASONING.get(), SoundSource.PLAYERS, RandomSource.create());
        this.player = player;
        this.volume = 0.8F;
        this.looping = false;
        tick();
    }

    @Override public void tick() {
        if (!player.isUsingItem() || !player.getUseItem().is(ModItems.PENDING_SEASONING.get())) {
            stop();
            return;
        }
        x = player.getX();
        y = player.getY() + 1.0;
        z = player.getZ();
    }

    public void end() { stop(); }
}
