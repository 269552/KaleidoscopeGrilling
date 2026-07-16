package cn.breezeth.kaleidoscope_grilling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class SkeweringHandler {
    private static final String INGREDIENTS_TAG = "SkewerIngredients";
    private static final String INGREDIENT_STACKS_TAG = "SkewerIngredientStacks";
    private static final String VARIANTS_TAG = "SkewerModelVariants";

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack food = event.getEntity().getMainHandItem();
        ItemStack offhand = event.getEntity().getOffhandItem();
        if (event.getEntity().isShiftKeyDown()) {
            InteractionHand skewerHand = canDisassemble(food) ? InteractionHand.MAIN_HAND
                    : canDisassemble(offhand) ? InteractionHand.OFF_HAND : null;
            if (skewerHand == null) return;
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            if (!event.getLevel().isClientSide
                    && disassemble(event.getEntity().getItemInHand(skewerHand), event.getEntity(), skewerHand))
                event.getLevel().playSound(null, event.getEntity().blockPosition(), ModSounds.SKEWER_DISASSEMBLE.get(),
                        SoundSource.PLAYERS, 0.8F, 1.0F);
            return;
        }
        if (!offhand.is(Items.STICK) && !offhand.is(ModItems.UNFINISHED_SKEWER.get())
                && !(offhand.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(offhand))) return;

        List<ItemStack> insertedStacks = readIngredientStacks(offhand);
        ResourceLocation foodId = ForgeRegistries.ITEMS.getKey(food.getItem());
        if (foodId == null) return;
        if (!SkewerRecipes.canAppend(insertedStacks, food) && !SkewerRecipes.isConfiguredIngredient(food)
                && !SkewerCompatApi.canSkewer(food, event.getEntity())) return;
        if (insertedStacks.size() >= 3) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (event.getLevel().isClientSide) return;

        insertedStacks.add(food.copyWithCount(1));
        List<Integer> variants = readVariants(offhand);
        variants.add(event.getEntity().getRandom().nextInt(3) + 1);
        ResourceLocation resultId = SkewerRecipes.completedResult(insertedStacks);
        ItemStack next;
        if (resultId != null) {
            next = new ItemStack(ForgeRegistries.ITEMS.getValue(resultId));
            write(next, insertedStacks, variants);
        } else if (insertedStacks.size() >= 3) {
            next = new ItemStack(ModItems.SECRET_SKEWER.get());
            write(next, insertedStacks, variants);
            SecretSkewerItem.setCreator(next, event.getEntity());
        } else {
            next = new ItemStack(ModItems.UNFINISHED_SKEWER.get());
            write(next, insertedStacks, variants);
        }
        if (offhand.is(Items.STICK) && (offhand.getCount() > 1 || event.getEntity().getAbilities().instabuild)) {
            ItemStack remainingSticks = offhand.copy();
            remainingSticks.setCount(event.getEntity().getAbilities().instabuild ? offhand.getCount() : offhand.getCount() - 1);
            event.getEntity().getInventory().placeItemBackInInventory(remainingSticks);
        }
        if (!event.getEntity().getAbilities().instabuild) food.shrink(1);
        event.getEntity().setItemInHand(InteractionHand.OFF_HAND, next);
        event.getLevel().playSound(null, event.getEntity().blockPosition(), ModSounds.ACTION_SUCCESS.get(),
                SoundSource.PLAYERS, 0.7F, 1.0F);
    }

    private static List<String> read(ItemStack stack) {
        return ids(readIngredientStacks(stack));
    }

    static List<ItemStack> readIngredientStacks(ItemStack stack) {
        List<ItemStack> result = new ArrayList<>();
        if (!stack.hasTag()) return result;
        ListTag stacks = stack.getTag().getList(INGREDIENT_STACKS_TAG, 10);
        for (int i = 0; i < stacks.size(); i++) result.add(ItemStack.of(stacks.getCompound(i)));
        if (!result.isEmpty()) return result;
        ListTag legacy = stack.getTag().getList(INGREDIENTS_TAG, 8);
        for (int i = 0; i < legacy.size(); i++) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(legacy.getString(i)));
            if (item != null) result.add(new ItemStack(item));
        }
        return result;
    }

    private static List<String> ids(List<ItemStack> stacks) {
        List<String> result = new ArrayList<>();
        for (ItemStack ingredient : stacks) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(ingredient.getItem());
            if (id != null) result.add(id.toString());
        }
        return result;
    }

    private static List<Integer> readVariants(ItemStack stack) {
        List<Integer> result = new ArrayList<>();
        if (!stack.hasTag()) return result;
        int[] values = stack.getTag().getIntArray(VARIANTS_TAG);
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

    static ItemStack popLast(ItemStack stack) {
        List<ItemStack> ingredients = readIngredientStacks(stack);
        if (ingredients.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = ingredients.remove(ingredients.size() - 1);
        List<Integer> variants = readVariants(stack);
        if (!variants.isEmpty()) variants.remove(variants.size() - 1);
        write(stack, ingredients, variants);
        return removed;
    }

    static boolean disassemble(ItemStack stack, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        List<ItemStack> ingredients = readIngredientStacks(stack);
        if (ingredients.isEmpty()) return false;
        for (ItemStack ingredient : ingredients)
            player.getInventory().placeItemBackInInventory(ingredient.copyWithCount(1));
        player.getInventory().placeItemBackInInventory(new ItemStack(Items.STICK));
        stack.shrink(1);
        if (stack.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
        return true;
    }

    private static boolean canDisassemble(ItemStack stack) {
        return stack.is(ModItems.UNFINISHED_SKEWER.get()) || SkewerRecipes.isRawSkewer(stack)
                || stack.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(stack);
    }

    static ItemStack finishAsSecret(ItemStack stack, net.minecraft.world.entity.player.Player player) {
        List<ItemStack> ingredients = readIngredientStacks(stack);
        if (ingredients.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(ModItems.SECRET_SKEWER.get());
        write(result, ingredients, readVariants(stack));
        SecretSkewerItem.setCreator(result, player);
        return result;
    }

    private static void write(ItemStack stack, List<ItemStack> ingredients, List<Integer> variants) {
        ListTag list = new ListTag();
        ListTag stackList = new ListTag();
        for (ItemStack ingredient : ingredients) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(ingredient.getItem());
            if (id != null) list.add(StringTag.valueOf(id.toString()));
            stackList.add(ingredient.copyWithCount(1).save(new CompoundTag()));
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(INGREDIENTS_TAG, list);
        tag.put(INGREDIENT_STACKS_TAG, stackList);
        tag.putIntArray(VARIANTS_TAG, variants);
    }

    private SkeweringHandler() {}
}
