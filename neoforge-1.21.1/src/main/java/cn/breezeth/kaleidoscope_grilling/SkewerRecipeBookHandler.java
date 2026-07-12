package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class SkewerRecipeBookHandler {
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!result.is(ModItems.SKEWER_RECIPE_BOOK.get())) return;
        var grid = event.getInventory();
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack slot = grid.getItem(i);
            if (slot.isEmpty()) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(slot.getItem());
            if (id != null && id.getNamespace().equals(KaleidoscopeGrilling.MOD_ID)
                    && id.getPath().startsWith("raw_")) {
                SkewerRecipeBookItem.setRecipeResult(result, id.toString());
                return;
            }
        }
    }

    private SkewerRecipeBookHandler() {}
}
