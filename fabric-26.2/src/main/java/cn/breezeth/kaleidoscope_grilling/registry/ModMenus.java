package cn.breezeth.kaleidoscope_grilling.registry;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.fabric.registry.RegistryRef;
import cn.breezeth.kaleidoscope_grilling.rack.AdvancedRackMenu;
import cn.breezeth.kaleidoscope_grilling.rack.RackSelectionMenu;
import cn.breezeth.kaleidoscope_grilling.rack.RackShortcutMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
  public static final RegistryRef<MenuType<AdvancedRackMenu>> ADVANCED_RACK =
      register("advanced_rack", new MenuType<>(AdvancedRackMenu::new, FeatureFlags.VANILLA_SET));
  public static final RegistryRef<MenuType<RackSelectionMenu>> RACK_SELECTION =
      register("rack_selection", new MenuType<>(RackSelectionMenu::new, FeatureFlags.VANILLA_SET));
  public static final RegistryRef<MenuType<RackShortcutMenu>> RACK_SHORTCUT =
      register("rack_shortcut", new MenuType<>(RackShortcutMenu::new, FeatureFlags.VANILLA_SET));

  private static <T extends AbstractContainerMenu> RegistryRef<MenuType<T>> register(
      String name, MenuType<T> type) {
    Identifier id = Identifier.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, name);
    ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU, id);
    return RegistryRef.of(Registry.register(BuiltInRegistries.MENU, key, type));
  }

  public static void init() {}

  private ModMenus() {}
}
