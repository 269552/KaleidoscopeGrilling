package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
  public static final RegistryRef<SoundEvent> ACTION_SUCCESS = register("action_success");
  public static final RegistryRef<SoundEvent> GRILL_FLIP = register("grill_flip");
  public static final RegistryRef<SoundEvent> GRILL_LOOP = register("grill_loop");
  public static final RegistryRef<SoundEvent> PICKUP_ITEM = register("pickup_item");
  public static final RegistryRef<SoundEvent> SKEWER_DISASSEMBLE = register("skewer_disassemble");
  public static final RegistryRef<SoundEvent> SEASON = register("season");
  public static final RegistryRef<SoundEvent> SHAKE_SEASONING = register("shake_seasoning");
  public static final RegistryRef<SoundEvent> SEASONING_BOTTLE_PLACE = register("seasoning_bottle_place");
  public static final RegistryRef<SoundEvent> SEASONING_BOTTLE_STACK = register("seasoning_bottle_stack");
  public static final RegistryRef<SoundEvent> ONE_SKEWER_EAT = register("one_skewer_eat");
  public static final RegistryRef<SoundEvent> TWO_SKEWER_EAT = register("two_skewer_eat");
  public static final RegistryRef<SoundEvent> THREE_SKEWER_EAT = register("three_skewer_eat");
  public static final RegistryRef<SoundEvent> FOUR_SKEWER_EAT = register("four_skewer_eat");

  private static RegistryRef<SoundEvent> register(String name) {
    Identifier id = Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name);
    ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT, id);
    SoundEvent sound = SoundEvent.createVariableRangeEvent(id);
    return RegistryRef.of(Registry.register(BuiltInRegistries.SOUND_EVENT, key, sound));
  }

  /** Forces class initialization from the Fabric entrypoint. */
  public static void init() {}

  private ModSounds() {}
}
