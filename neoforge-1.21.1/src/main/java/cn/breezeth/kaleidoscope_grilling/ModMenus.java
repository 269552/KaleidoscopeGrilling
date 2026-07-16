package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, KaleidoscopeGrilling.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedRackMenu>> ADVANCED_RACK = MENUS.register("advanced_rack",
            () -> new MenuType<>(AdvancedRackMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<RackSelectionMenu>> RACK_SELECTION = MENUS.register("rack_selection",
            () -> new MenuType<>(RackSelectionMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<RackShortcutMenu>> RACK_SHORTCUT = MENUS.register("rack_shortcut",
            () -> new MenuType<>(RackShortcutMenu::new, FeatureFlags.VANILLA_SET));
    private ModMenus() {}
}
