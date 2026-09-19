package cn.breezeth.kaleidoscope_grilling.kubejs;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

/** Optional KubeJS bridge. The core mod remains fully usable without KubeJS. */
public final class GrillingKubeJSPlugin implements KubeJSPlugin {
  @Override
  public void registerBuilderTypes(BuilderTypeRegistry registry) {
    registry.of(
        Registries.ITEM,
        types -> {
          types.add(
              ResourceLocation.fromNamespaceAndPath("kaleidoscope_grilling", "raw_skewer"),
              KubeSkewerItemBuilder.class,
              id -> new KubeSkewerItemBuilder(id, true));
          types.add(
              ResourceLocation.fromNamespaceAndPath("kaleidoscope_grilling", "cooked_skewer"),
              KubeSkewerItemBuilder.class,
              id -> new KubeSkewerItemBuilder(id, false));
        });
  }

  @Override
  public void registerBindings(BindingRegistry bindings) {
    if (bindings.type() == ScriptType.SERVER)
      bindings.add("Grilling", GrillingKubeJSBindings.class);
  }

  @Override
  public void afterScriptsLoaded(ScriptManager manager) {
    if (manager.scriptType == ScriptType.SERVER)
      GrillingDataManager.commitScriptThreadingRecipes();
  }
}
