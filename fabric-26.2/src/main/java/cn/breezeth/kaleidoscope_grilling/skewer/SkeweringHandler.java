package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.registry.ModAdvancements;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;
import cn.breezeth.kaleidoscope_grilling.SkewerCompatApi;

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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public final class SkeweringHandler {
  private static final String INGREDIENTS_TAG = "SkewerIngredients";
  private static final String INGREDIENT_STACKS_TAG = "SkewerIngredientStacks";
  private static final String COOKED_INGREDIENT_STACKS_TAG = "CookedIngredientStacks";
  private static final String VARIANTS_TAG = "SkewerModelVariants";
  private static final String CREATIVE_PREVIEW_TAG = "CreativeSkewerPreview";
  private static final int GUI_VARIANT_OFFSET = 4;
  private static final int GUI_VARIANT_COUNT = 6;

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
    variants.add(event.getEntity().getRandom().nextInt(GUI_VARIANT_COUNT) + GUI_VARIANT_OFFSET);
    ResourceLocation resultId = SkewerRecipes.completedResult(insertedStacks);
    ResourceLocation threadingResult = SkewerRecipes.threadingResult(insertedStacks);
    ItemStack next;
    if (threadingResult != null && BuiltInRegistries.ITEM.containsKey(threadingResult)) {
      next = new ItemStack(BuiltInRegistries.ITEM.get(threadingResult));
    } else if (resultId != null && BuiltInRegistries.ITEM.containsKey(resultId)) {
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
    ItemStack remainingSticks = ItemStack.EMPTY;
    if (offhand.is(Items.STICK)) {
      remainingSticks = offhand.copy();
      if (!event.getEntity().getAbilities().instabuild) remainingSticks.shrink(1);
    }
    if (!event.getEntity().getAbilities().instabuild) food.shrink(1);
    event.getEntity().setItemInHand(InteractionHand.OFF_HAND, next);
    if (!remainingSticks.isEmpty())
      event.getEntity().getInventory().placeItemBackInInventory(remainingSticks);
    if (threadingResult != null || resultId != null || insertedStacks.size() >= 3)
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
    ListTag list = data.getUnsafe().getList(INGREDIENTS_TAG, 8);
    for (int i = 0; i < list.size(); i++) result.add(list.getString(i));
    return result;
  }

  public static List<ItemStack> readIngredientStacks(
      ItemStack stack, HolderLookup.Provider registries) {
    return readStackList(stack, INGREDIENT_STACKS_TAG, registries, true);
  }

  static List<ItemStack> readEffectiveIngredientStacks(
      ItemStack stack, HolderLookup.Provider registries) {
    if (SecretSkewerItem.isCooked(stack) || SecretSkewerItem.getVisualStage(stack) >= 4) {
      List<ItemStack> cooked =
          readStackList(stack, COOKED_INGREDIENT_STACKS_TAG, registries, false);
      if (!cooked.isEmpty()) return cooked;
    }
    return readIngredientStacks(stack, registries);
  }

  static boolean hasCookedIngredientStacks(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null
        && !data.getUnsafe().getList(COOKED_INGREDIENT_STACKS_TAG, 10).isEmpty();
  }

  public static void ensureCookedIngredientStacks(ItemStack stack, Level level) {
    HolderLookup.Provider registries = level.registryAccess();
    if (!stack.is(ModItems.SECRET_SKEWER.get())
        || level.isClientSide
        || hasCookedIngredientStacks(stack)) return;
    List<ItemStack> cooked = new ArrayList<>();
    for (ItemStack ingredient : readIngredientStacks(stack, registries)) {
      ItemStack resolved = ingredient.copyWithCount(1);
      SingleRecipeInput input = new SingleRecipeInput(resolved);
      level
          .getRecipeManager()
          .getRecipeFor(RecipeType.SMOKING, input, level)
          .ifPresent(
              holder -> {
                ItemStack result = holder.value().assemble(input, registries);
                if (!result.isEmpty() && result.has(DataComponents.FOOD)) {
                  resolved.setCount(0);
                  cooked.add(result.copyWithCount(1));
                }
              });
      if (!resolved.isEmpty()) cooked.add(resolved);
    }
    if (!cooked.isEmpty())
      writeStackList(stack, COOKED_INGREDIENT_STACKS_TAG, cooked, registries);
  }

  private static List<ItemStack> readStackList(
      ItemStack stack,
      String key,
      HolderLookup.Provider registries,
      boolean legacyFallback) {
    List<ItemStack> result = new ArrayList<>();
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data == null) return result;
    CompoundTag tag = data.getUnsafe();
    ListTag stacks = tag.getList(key, 10);
    for (int i = 0; i < stacks.size(); i++)
      result.add(ItemStack.parseOptional(registries, stacks.getCompound(i)));
    if (!result.isEmpty() || !legacyFallback) return result;
    for (String id : read(stack))
      result.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
    return result;
  }

  private static void writeStackList(
      ItemStack stack,
      String key,
      List<ItemStack> ingredients,
      HolderLookup.Provider registries) {
    ListTag list = new ListTag();
    for (ItemStack ingredient : ingredients)
      list.add(ingredient.copyWithCount(1).saveOptional(registries));
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.put(key, list);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
    int[] values = data.getUnsafe().getIntArray(VARIANTS_TAG);
    for (int value : values) result.add(value);
    return result;
  }

  static List<String> readIngredients(ItemStack stack) {
    return read(stack);
  }

  public static int ingredientCount(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? 0 : data.getUnsafe().getList(INGREDIENTS_TAG, 8).size();
  }

  /** Stores configured ingredients on a generated cooked skewer. */
  public static void writeGeneratedIngredients(ItemStack stack, List<ItemStack> ingredients) {
    List<ItemStack> values =
        ingredients.stream()
            .filter(item -> !item.isEmpty())
            .limit(3)
            .map(item -> item.copyWithCount(1))
            .toList();
    ListTag ids = new ListTag();
    List<Integer> variants = new ArrayList<>(values.size());
    int seed = BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
    for (int index = 0; index < values.size(); index++) {
      ids.add(
          StringTag.valueOf(BuiltInRegistries.ITEM.getKey(values.get(index).getItem()).toString()));
      variants.add(GUI_VARIANT_OFFSET + Math.floorMod(seed + index * 31, GUI_VARIANT_COUNT));
    }
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.put(INGREDIENTS_TAG, ids);
    tag.putIntArray(VARIANTS_TAG, variants);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static int modelState(ItemStack stack) {
    List<Integer> variants = readVariants(stack);
    if (variants.isEmpty()) {
      List<ItemStack> fixed = SkewerRecipes.displayIngredients(stack);
      int hash = BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
      for (int i = 0; i < fixed.size(); i++) variants.add(1 + Math.floorMod(hash + i * 31, 3));
    }
    int first = variants.size() > 0 ? modelVariant(variants.get(0)) : 0;
    int second = variants.size() > 1 ? modelVariant(variants.get(1)) : 0;
    int third = variants.size() > 2 ? modelVariant(variants.get(2)) : 0;
    return first * 16 + second * 4 + third;
  }

  static int guiVariantBits(ItemStack stack) {
    List<Integer> variants = readVariants(stack);
    List<String> ingredients = read(stack);
    int bits = 0;
    for (int slot = 0; slot < 3; slot++) {
      int value = slot < variants.size() ? variants.get(slot) : 0;
      int bit;
      if (value >= GUI_VARIANT_OFFSET && value < GUI_VARIANT_OFFSET + GUI_VARIANT_COUNT) {
        bit = (value - GUI_VARIANT_OFFSET) / 3;
      } else {
        int hash = 31 * (slot + 1) + value;
        if (slot < ingredients.size()) hash = 31 * hash + ingredients.get(slot).hashCode();
        hash ^= hash >>> 16;
        hash *= 0x7FEB352D;
        hash ^= hash >>> 15;
        bit = hash & 1;
      }
      bits |= bit << slot;
    }
    return bits;
  }

  private static int modelVariant(int value) {
    if (value >= GUI_VARIANT_OFFSET && value < GUI_VARIANT_OFFSET + GUI_VARIANT_COUNT)
      return (value - GUI_VARIANT_OFFSET) % 3 + 1;
    return value >= 1 && value <= 3 ? value : 0;
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

  /**
   * Appends a food item to an existing skewer (stick, unfinished skewer, or uncooked secret skewer).
   * Player-independent, for mechanical (Create Deployer) contexts; player may be null.
   *
   * @return the resulting skewer ItemStack, or {@link ItemStack#EMPTY} if the food cannot be appended.
   */
  public static ItemStack appendToSkewer(
      ItemStack skewer,
      ItemStack food,
      net.minecraft.util.RandomSource random,
      @Nullable Player player,
      HolderLookup.Provider registries) {
    List<ItemStack> insertedStacks = readIngredientStacks(skewer, registries);
    if (food.isEmpty() || insertedStacks.size() >= 3) return ItemStack.EMPTY;
    if (!SkewerRecipes.canAppend(insertedStacks, food)
        && !SkewerRecipes.isConfiguredIngredient(food)
        && !SkewerCompatApi.canSkewer(food, player)) return ItemStack.EMPTY;

    insertedStacks.add(food.copyWithCount(1));
    ResourceLocation resultId = SkewerRecipes.completedResult(insertedStacks);
    ResourceLocation threadingResult = SkewerRecipes.threadingResult(insertedStacks);
    ItemStack next;
    if (threadingResult != null && BuiltInRegistries.ITEM.containsKey(threadingResult)) {
      next = new ItemStack(BuiltInRegistries.ITEM.get(threadingResult));
    } else if (resultId != null && BuiltInRegistries.ITEM.containsKey(resultId)) {
      // 固定串模型固定、无变体：确定性 variants，保证同食材组合可堆叠
      List<Integer> fixedVariants = new ArrayList<>();
      for (int i = 0; i < insertedStacks.size(); i++)
        fixedVariants.add(GUI_VARIANT_OFFSET);
      next = new ItemStack(BuiltInRegistries.ITEM.get(resultId));
      write(next, insertedStacks, fixedVariants, registries);
    } else {
      List<Integer> variants = readVariants(skewer);
      variants.add(random.nextInt(GUI_VARIANT_COUNT) + GUI_VARIANT_OFFSET);
      if (insertedStacks.size() >= 3) {
        next = new ItemStack(ModItems.SECRET_SKEWER.get());
        write(next, insertedStacks, variants, registries);
        if (player != null) SecretSkewerItem.setCreator(next, player);
      } else {
        next = new ItemStack(ModItems.UNFINISHED_SKEWER.get());
        write(next, insertedStacks, variants, registries);
      }
    }
    return next;
  }

  static ItemStack finishAsSecret(
      ItemStack stack,
      net.minecraft.world.entity.player.Player player,
      HolderLookup.Provider registries) {
    List<ItemStack> ingredients = readIngredientStacks(stack, registries);
    if (ingredients.size() != 3) return ItemStack.EMPTY;
    ItemStack result = new ItemStack(ModItems.SECRET_SKEWER.get());
    write(result, ingredients, readVariants(stack), registries);
    SecretSkewerItem.setCreator(result, player);
    return result;
  }

  public static ItemStack jeiSecretSkewer(List<ItemStack> ingredients, boolean cooked) {
    ItemStack result = new ItemStack(ModItems.SECRET_SKEWER.get());
    ListTag ids = new ListTag();
    for (ItemStack ingredient : ingredients)
      ids.add(
          StringTag.valueOf(BuiltInRegistries.ITEM.getKey(ingredient.getItem()).toString()));
    CompoundTag tag = new CompoundTag();
    tag.put(INGREDIENTS_TAG, ids);
    tag.putIntArray(VARIANTS_TAG, new int[] {4, 8, 6});
    result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    SecretSkewerItem.setCooked(result, cooked);
    return result;
  }

  public static ItemStack creativePreviewSkewer() {
    ItemStack result = jeiSecretSkewer(creativePreviewIngredients(0), false);
    CompoundTag tag =
        result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putBoolean(CREATIVE_PREVIEW_TAG, true);
    result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return result;
  }

  static ItemStack creativePreviewFrame(ItemStack stack, long gameTime) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (!stack.is(ModItems.SECRET_SKEWER.get())
        || data == null
        || !data.copyTag().getBoolean(CREATIVE_PREVIEW_TAG)) return stack;
    return jeiSecretSkewer(creativePreviewIngredients((int) (gameTime / 40L % 3L)), false);
  }

  private static List<ItemStack> creativePreviewIngredients(int frame) {
    return switch (frame) {
      case 1 ->
          List.of(
              new ItemStack(ModItems.CHICKEN_WING.get()),
              new ItemStack(ModItems.ONION.get()),
              new ItemStack(Items.POTATO));
      case 2 ->
          List.of(
              new ItemStack(ModItems.SQUID_TENTACLE.get()),
              new ItemStack(Items.CARROT),
              new ItemStack(ModItems.HOUTTUYNIA.get()));
      default ->
          List.of(
              new ItemStack(Items.APPLE),
              new ItemStack(ModItems.BEEF_CHUNKS.get()),
              new ItemStack(Items.BROWN_MUSHROOM));
    };
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
