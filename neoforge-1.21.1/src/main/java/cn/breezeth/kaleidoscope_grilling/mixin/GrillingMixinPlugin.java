package cn.breezeth.kaleidoscope_grilling.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class GrillingMixinPlugin implements IMixinConfigPlugin {
  private static final boolean CREATE_AVAILABLE = classExists("com.simibubi.create.Create");
  private static final boolean MAID_AVAILABLE =
      classExists("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid");

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    if (mixinClassName.endsWith("PressingBehaviourMixin")
        || mixinClassName.endsWith("BeltDeployerCallbacksMixin")
        || mixinClassName.endsWith("BasinOperatingBlockEntityAccessor")
        || mixinClassName.endsWith("MechanicalMixerSeasoningMixin")
        || mixinClassName.endsWith("BasinRecipeSeasoningMixin")) return CREATE_AVAILABLE;
    if (mixinClassName.endsWith("WirelessIOBaubleMixin")
        || mixinClassName.endsWith("WirelessIOItemMixin")
        || mixinClassName.endsWith("WirelessIOContainerGuiMixin")
        || mixinClassName.endsWith("MaidRendererGrillingLayerMixin")
        || mixinClassName.endsWith("GeckoMaidRendererGrillingLayerMixin")) return MAID_AVAILABLE;
    return true;
  }

  private static boolean classExists(String name) {
    String resource = name.replace('.', '/') + ".class";
    return GrillingMixinPlugin.class.getClassLoader().getResource(resource) != null;
  }

  @Override public void onLoad(String mixinPackage) {}
  @Override public String getRefMapperConfig() { return null; }
  @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
  @Override public List<String> getMixins() { return null; }
  @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
  @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
