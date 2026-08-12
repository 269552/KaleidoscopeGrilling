package cn.breezeth.kaleidoscope_grilling.client;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;


import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class ClientSkewerEatingSound {
  private static final Map<Integer, EntityBoundSoundInstance> ACTIVE = new HashMap<>();

  public static void handle(
      int entityId, boolean playing, MultiBiteSkewerItem.AnimationProfile profile) {
    Minecraft minecraft = Minecraft.getInstance();
    EntityBoundSoundInstance previous = ACTIVE.remove(entityId);
    if (previous != null) minecraft.getSoundManager().stop(previous);
    if (!playing || minecraft.level == null) return;

    Entity entity = minecraft.level.getEntity(entityId);
    if (entity == null) return;
    EntityBoundSoundInstance sound =
        new EntityBoundSoundInstance(
            profile == MultiBiteSkewerItem.AnimationProfile.BEEF
                ? ModSounds.FOUR_SKEWER_EAT.get()
                : ModSounds.THREE_SKEWER_EAT.get(),
            SoundSource.PLAYERS,
            1.0F,
            1.0F,
            entity,
            minecraft.level.random.nextLong());
    ACTIVE.put(entityId, sound);
    minecraft.getSoundManager().play(sound);
  }

  @SubscribeEvent
  public static void clientTick(ClientTickEvent.Post event) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player == null) return;
    EntityBoundSoundInstance sound = ACTIVE.get(minecraft.player.getId());
    if (sound == null) return;
    boolean stillEating =
        minecraft.player.isUsingItem()
            && minecraft.player.getUseItem().getItem() instanceof MultiBiteSkewerItem;
    if (!stillEating)
      handle(
          minecraft.player.getId(),
          false,
          MultiBiteSkewerItem.AnimationProfile.RAW_ENDER_PEARL);
  }

  private ClientSkewerEatingSound() {}
}
