package cn.breezeth.kaleidoscope_grilling;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class SkeweringHandler {
  private static final String INGREDIENTS_TAG = "SkewerIngredients";
  private static final String INGREDIENT_STACKS_TAG = "SkewerIngredientStacks";
  private static final String VARIANTS_TAG = "SkewerModelVariants";

  public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
    ItemStack offhand = event.getEntity().getOffhandItem();
    if (event.getEntity().isShiftKeyDown()) {
      if (!canDisassemble(offhand)) return;
      event.setCancellationResult(InteractionResult.SUCCESS);
      event.setCanceled(true);
      if (!event.getLevel().isClientSide
          && disassemble(
              offhand,
              event.getEntity(),
              InteractionHand.OFF_HAND,
              event.getLevel().registryAccess()))
        event
            .getLevel()
            .playSound(
                null,
                event.getEntity().blockPosition(),
                ModSounds.SKEWER_DISASSEMBLE.get(),
                SoundSource.PLAYERS,
                0.8F,
                1.0F);
      return;
    }
    if (event.getHand() != InteractionHand.MAIN_HAND) return;
    ItemStack food = event.getEntity().getMainHandItem();
    if (!offhand.is(Items.STICK)
        && !offhand.is(ModItems.UNFINISHED_SKEWER.get())
        && !(offhand.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(offhand)))
      return;

    List<ItemStack> insertedStacks =
        readIngredientStacks(offhand, event.getLevel().registryAccess());
    ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(food.getItem());
    if (foodId == null) return;
    if (!SkewerRecipes.canAppend(insertedStacks, food)
        && !SkewerRecipes.isConfiguredIngredient(food)
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
      next = new ItemStack(BuiltInRegistries.ITEM.get(resultId));
      write(next, insertedStacks, variants, event.getLevel().registryAccess());
    } else if (insertedStacks.size() >= 3) {
      next = new ItemStack(ModItems.SECRET_SKEWER.get());
      write(next, insertedStacks, variants, event.getLevel().registryAccess());
      SecretSkewerItem.setCreator(next, event.getEntity());
    } else {
      next = new ItemStack(ModItems.UNFINISHED_SKEWER.get());
      write(next, insertedStacks, variants, event.getLevel().registryAccess());
    }
    if (offhand.is(Items.STICK)
        && (offhand.getCount() > 1 || event.getEntity().getAbilities().instabuild)) {
      ItemStack remainingSticks = offhand.copy();
      remainingSticks.setCount(
          event.getEntity().getAbilities().instabuild
              ? offhand.getCount()
              : offhand.getCount() - 1);
      event.getEntity().getInventory().placeItemBackInInventory(remainingSticks);
    }
    if (!event.getEntity().getAbilities().instabuild) food.shrink(1);
    event.getEntity().setItemInHand(InteractionHand.OFF_HAND, next);
    if (resultId != null || insertedStacks.size() >= 3)
      ModAdvancements.skewerCompleted(event.getEntity());
    event
        .getLevel()
        .playSound(
            null,
            event.getEntity().blockPosition(),
            ModSounds.ACTION_SUCCESS.get(),
            SoundSource.PLAYERS,
            0.7F,
            1.0F);
  }

  private static List<String> read(ItemStack stack) {
    List<String> result = new ArrayList<>();
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return result;
    ListTag list = data.copyTag().getList(INGREDIENTS_TAG, 8);
    for (int i = 0; i < list.size(); i++) result.add(list.getString(i));
    return result;
  }

  public static List<ItemStack> readIngredientStacks(
      ItemStack stack, HolderLookup.Provider registries) {
    List<ItemStack> result = new ArrayList<>();
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return result;
    CompoundTag tag = data.copyTag();
    ListTag stacks = tag.getList(INGREDIENT_STACKS_TAG, 10);
    for (int i = 0; i < stacks.size(); i++)
      result.add(ItemStack.parseOptional(registries, stacks.getCompound(i)));
    if (!result.isEmpty()) return result;
    for (String id : read(stack))
      result.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
    return result;
  }

  private static List<String> ids(List<ItemStack> stacks) {
    List<String> result = new ArrayList<>();
    for (ItemStack ingredient : stacks)
      result.add(BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString());
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
    if (variants.isEmpty()) {
      List<ItemStack> fixed = SkewerRecipes.displayIngredients(stack);
      int hash = BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
      for (int i = 0; i < fixed.size(); i++) variants.add(1 + Math.floorMod(hash + i * 31, 3));
    }
    int first = variants.size() > 0 ? variants.get(0) : 0;
    int second = variants.size() > 1 ? variants.get(1) : 0;
    int third = variants.size() > 2 ? variants.get(2) : 0;
    return first * 16 + second * 4 + third;
  }

  static ItemStack popLast(ItemStack stack, HolderLookup.Provider registries) {
    List<ItemStack> ingredients = readIngredientStacks(stack, registries);
    if (ingredients.isEmpty()) return ItemStack.EMPTY;
    ItemStack removed = ingredients.remove(ingredients.size() - 1);
    List<Integer> variants = readVariants(stack);
    if (!variants.isEmpty()) variants.remove(variants.size() - 1);
    write(stack, ingredients, variants, registries);
    return removed;
  }

  public static boolean disassemble(
      ItemStack stack, Player player, InteractionHand hand, HolderLookup.Provider registries) {
    List<ItemStack> ingredients = readIngredientStacks(stack, registries);
    if (ingredients.isEmpty()) return false;
    for (ItemStack ingredient : ingredients)
      player.getInventory().placeItemBackInInventory(ingredient.copyWithCount(1));
    player.getInventory().placeItemBackInInventory(new ItemStack(Items.STICK));
    stack.shrink(1);
    if (stack.isEmpty()) player.setItemInHand(hand, ItemStack.EMPTY);
    return true;
  }

  public static boolean canDisassemble(ItemStack stack) {
    return stack.is(ModItems.UNFINISHED_SKEWER.get())
        || SkewerRecipes.isRawSkewer(stack)
        || stack.is(ModItems.SECRET_SKEWER.get()) && !SecretSkewerItem.isCooked(stack);
  }

  static ItemStack finishAsSecret(
      ItemStack stack,
      net.minecraft.world.entity.player.Player player,
      HolderLookup.Provider registries) {
    List<ItemStack> ingredients = readIngredientStacks(stack, registries);
    if (ingredients.isEmpty()) return ItemStack.EMPTY;
    ItemStack result = new ItemStack(ModItems.SECRET_SKEWER.get());
    write(result, ingredients, readVariants(stack), registries);
    SecretSkewerItem.setCreator(result, player);
    return result;
  }

  static ItemStack jeiSecretSkewer(List<ItemStack> ingredients, boolean cooked) {
    ItemStack result = new ItemStack(ModItems.SECRET_SKEWER.get());
    ListTag ids = new ListTag();
    for (ItemStack ingredient : ingredients)
      ids.add(
          StringTag.valueOf(BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString()));
    CompoundTag tag = new CompoundTag();
    tag.put(INGREDIENTS_TAG, ids);
    tag.putIntArray(VARIANTS_TAG, new int[] {1, 2, 3});
    result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    SecretSkewerItem.setCooked(result, cooked);
    return result;
  }

  private static void write(
      ItemStack stack,
      List<ItemStack> ingredients,
      List<Integer> variants,
      HolderLookup.Provider registries) {
    ListTag list = new ListTag();
    ListTag stackList = new ListTag();
    for (ItemStack ingredient : ingredients) {
      list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString()));
      stackList.add(ingredient.copyWithCount(1).saveOptional(registries));
    }
    CompoundTag tag = new CompoundTag();
    tag.put(INGREDIENTS_TAG, list);
    tag.put(INGREDIENT_STACKS_TAG, stackList);
    tag.putIntArray(VARIANTS_TAG, variants);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  private SkeweringHandler() {}
}
