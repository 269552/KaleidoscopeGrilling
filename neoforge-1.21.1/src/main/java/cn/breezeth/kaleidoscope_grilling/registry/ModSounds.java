package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
  public static final DeferredRegister<SoundEvent> SOUNDS =
      DeferredRegister.create(Registries.SOUND_EVENT, KaleidoscopeGrilling.MOD_ID);
  public static final DeferredHolder<SoundEvent, SoundEvent> ACTION_SUCCESS =
      register("action_success");
  public static final DeferredHolder<SoundEvent, SoundEvent> GRILL_FLIP = register("grill_flip");
  public static final DeferredHolder<SoundEvent, SoundEvent> GRILL_LOOP = register("grill_loop");
  public static final DeferredHolder<SoundEvent, SoundEvent> PICKUP_ITEM = register("pickup_item");
  public static final DeferredHolder<SoundEvent, SoundEvent> SKEWER_DISASSEMBLE =
      register("skewer_disassemble");
  public static final DeferredHolder<SoundEvent, SoundEvent> SEASON = register("season");
  public static final DeferredHolder<SoundEvent, SoundEvent> SHAKE_SEASONING =
      register("shake_seasoning");
  public static final DeferredHolder<SoundEvent, SoundEvent> SEASONING_BOTTLE_PLACE =
      register("seasoning_bottle_place");
  public static final DeferredHolder<SoundEvent, SoundEvent> SEASONING_BOTTLE_STACK =
      register("seasoning_bottle_stack");
  public static final DeferredHolder<SoundEvent, SoundEvent> THREE_SKEWER_EAT =
      register("three_skewer_eat");
  public static final DeferredHolder<SoundEvent, SoundEvent> FOUR_SKEWER_EAT =
      register("four_skewer_eat");

  private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name);
    return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
  }

  private ModSounds() {}
}
