package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class SkeweringHandler {
    private static final String INGREDIENTS_TAG = "SkewerIngredients";
    private static final String VARIANTS_TAG = "SkewerModelVariants";

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack food = event.getEntity().getMainHandItem();
        ItemStack offhand = event.getEntity().getOffhandItem();
        if (!offhand.is(Items.STICK) && !offhand.is(ModItems.UNFINISHED_SKEWER.get())) return;

        List<String> inserted = read(offhand);
        ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(food.getItem());
        if (foodId == null) return;
        boolean matchesRecipe = SkewerRecipes.canAppend(inserted, foodId.toString());
        if (!matchesRecipe && (inserted.isEmpty() || inserted.size() >= 3)) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getLevel().isClientSide) return;

        inserted.add(foodId.toString());
        List<Integer> variants = readVariants(offhand);
        variants.add(event.getEntity().getRandom().nextInt(3) + 1);
        String resultId = SkewerRecipes.completedResult(inserted);
        ItemStack next;
        if (resultId != null) {
            next = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(KaleidoscopeGrilling.MOD_ID, resultId)));
            write(next, inserted, variants);
        } else if (inserted.size() >= 3) {
            next = new ItemStack(ModItems.SECRET_SKEWER.get());
            write(next, inserted, variants);
            SecretSkewerItem.setCreator(next, event.getEntity().getScoreboardName());
        } else {
            next = new ItemStack(ModItems.UNFINISHED_SKEWER.get());
            write(next, inserted, variants);
        }
        if (!event.getEntity().getAbilities().instabuild) food.shrink(1);
        event.getEntity().setItemInHand(InteractionHand.OFF_HAND, next);
    }

    private static List<String> read(ItemStack stack) {
        List<String> result = new ArrayList<>();
        if (!stack.is(ModItems.UNFINISHED_SKEWER.get())) return result;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return result;
        ListTag list = data.copyTag().getList(INGREDIENTS_TAG, 8);
        for (int i = 0; i < list.size(); i++) result.add(list.getString(i));
        return result;
    }

    private static List<Integer> readVariants(ItemStack stack) {
        List<Integer> result = new ArrayList<>();
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return result;
        int[] values = data.copyTag().getIntArray(VARIANTS_TAG);
        for (int value : values) result.add(value);
        return result;
    }

    static List<String> readIngredients(ItemStack stack) {
        return read(stack);
    }

    static int modelState(ItemStack stack) {
        List<Integer> variants = readVariants(stack);
        int first = variants.size() > 0 ? variants.get(0) : 0;
        int second = variants.size() > 1 ? variants.get(1) : 0;
        int third = variants.size() > 2 ? variants.get(2) : 0;
        return first * 16 + second * 4 + third;
    }

    private static void write(ItemStack stack, List<String> ingredients, List<Integer> variants) {
        ListTag list = new ListTag();
        ingredients.forEach(id -> list.add(StringTag.valueOf(id)));
        CompoundTag tag = new CompoundTag();
        tag.put(INGREDIENTS_TAG, list);
        tag.putIntArray(VARIANTS_TAG, variants);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private SkeweringHandler() {}
}
