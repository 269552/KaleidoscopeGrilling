package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class SkewerRecipeBookHandler {
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!result.is(ModItems.SKEWER_RECIPE_BOOK.get())) return;
        var grid = event.getInventory();
        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack slot = grid.getItem(i);
            if (slot.isEmpty()) continue;
            if (slot.is(ModItems.SECRET_SKEWER.get())
                    && SkeweringHandler.readIngredientStacks(slot).size() == 3) {
                SkewerRecipeBookItem.setRecipeStack(result, slot);
                return;
            }
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(slot.getItem());
            if (id != null && id.getNamespace().equals(KaleidoscopeGrilling.MOD_ID)
                    && id.getPath().startsWith("raw_")) {
                SkewerRecipeBookItem.setRecipeResult(result, id.toString());
                return;
            }
        }
    }

    private SkewerRecipeBookHandler() {}
}
