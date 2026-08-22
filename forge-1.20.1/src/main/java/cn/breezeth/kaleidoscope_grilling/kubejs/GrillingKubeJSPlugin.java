package cn.breezeth.kaleidoscope_grilling.kubejs;

import cn.breezeth.kaleidoscope_grilling.data.GrillingDataManager;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

/** Optional KubeJS bridge. The core mod remains fully usable without KubeJS. */
public final class GrillingKubeJSPlugin extends KubeJSPlugin {
  @Override
  public void init() {
    RegistryInfo.ITEM.addType(
        "kaleidoscope_grilling:raw_skewer",
        KubeSkewerItemBuilder.class,
        id -> new KubeSkewerItemBuilder(id, true));
    RegistryInfo.ITEM.addType(
        "kaleidoscope_grilling:cooked_skewer",
        KubeSkewerItemBuilder.class,
        id -> new KubeSkewerItemBuilder(id, false));
  }

  @Override
  public void onServerReload() {
    GrillingDataManager.commitScriptThreadingRecipes();
  }

  @Override
  public void registerBindings(BindingsEvent bindings) {
    if (bindings.getType() == ScriptType.SERVER)
      bindings.add("Grilling", GrillingKubeJSBindings.class);
  }
}
