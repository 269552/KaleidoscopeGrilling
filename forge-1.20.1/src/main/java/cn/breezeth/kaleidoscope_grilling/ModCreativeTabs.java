package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KaleidoscopeGrilling.MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.kaleidoscope_grilling.main"))
            .icon(() -> new ItemStack(ModBlocks.GRILL_ITEM.get()))
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());
    private ModCreativeTabs() {}
}
