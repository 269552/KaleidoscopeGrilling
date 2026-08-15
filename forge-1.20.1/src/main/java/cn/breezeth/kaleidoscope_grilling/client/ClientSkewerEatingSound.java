package cn.breezeth.kaleidoscope_grilling.client;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;

import cn.breezeth.kaleidoscope_grilling.skewer.MultiBiteSkewerItem;


import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = KaleidoscopeGrilling.MOD_ID, value = Dist.CLIENT)
public final class ClientSkewerEatingSound {
  private static final Map<Integer, EntityBoundSoundInstance> ACTIVE = new HashMap<>();
  private static final Map<Integer, MultiBiteSkewerItem.AnimationProfile> ACTIVE_PROFILES =
      new HashMap<>();

  public static void handle(
      int entityId, boolean playing, MultiBiteSkewerItem.AnimationProfile profile) {
    Minecraft minecraft = Minecraft.getInstance();
    EntityBoundSoundInstance previous = ACTIVE.remove(entityId);
    if (previous != null) minecraft.getSoundManager().stop(previous);
    if (!playing) {
      ACTIVE_PROFILES.remove(entityId);
      return;
    }
    ACTIVE_PROFILES.put(entityId, profile);
    if (minecraft.level == null) return;

    Entity entity = minecraft.level.getEntity(entityId);
    if (entity == null) return;
    SoundEvent soundEvent;
    switch (profile) {
      case ONE -> soundEvent = ModSounds.ONE_SKEWER_EAT.get();
      case TWO -> soundEvent = ModSounds.TWO_SKEWER_EAT.get();
      case FOUR -> soundEvent = ModSounds.FOUR_SKEWER_EAT.get();
      default -> soundEvent = ModSounds.THREE_SKEWER_EAT.get();
    }
    EntityBoundSoundInstance sound =
        new EntityBoundSoundInstance(
            soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F, entity, minecraft.level.random.nextLong());
    ACTIVE.put(entityId, sound);
    minecraft.getSoundManager().play(sound);
  }

  public static MultiBiteSkewerItem.AnimationProfile profile(
      int entityId, MultiBiteSkewerItem.AnimationProfile fallback) {
    return SkewerAnimationDebug.profile(
        entityId, ACTIVE_PROFILES.getOrDefault(entityId, fallback));
  }

  @SubscribeEvent
  public static void clientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) return;
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player == null) return;
    if (!ACTIVE_PROFILES.containsKey(minecraft.player.getId())) return;
    boolean stillEating =
        minecraft.player.isUsingItem()
            && minecraft.player.getUseItem().getItem() instanceof MultiBiteSkewerItem;
    if (!stillEating)
      handle(
          minecraft.player.getId(),
          false,
          MultiBiteSkewerItem.AnimationProfile.THREE);
  }

  private ClientSkewerEatingSound() {}
}
