package cn.breezeth.kaleidoscope_grilling.skewer;

import cn.breezeth.kaleidoscope_grilling.KaleidoscopeGrilling;
import cn.breezeth.kaleidoscope_grilling.registry.ModBlocks;
import cn.breezeth.kaleidoscope_grilling.registry.ModItems;
import cn.breezeth.kaleidoscope_grilling.registry.ModSounds;

import com.github.ysbbbbbb.kaleidoscopecookery.inventory.tooltip.RecipeItemTooltip;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class SkewerRecipeBookItem extends Item {
  private static final String RECIPE_RESULT_TAG = "RecipeResult";

  public SkewerRecipeBookItem(Properties properties) {
    super(properties);
  }

  public static String readRecipeResult(ItemStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    if (data != null) {
      CompoundTag tag = data.copyTag();
      if (tag.contains(RECIPE_RESULT_TAG)) {
        return tag.getString(RECIPE_RESULT_TAG);
      }
    }
    return "";
  }

  public static void setRecipeResult(ItemStack stack, String resultId) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    tag.putString(RECIPE_RESULT_TAG, resultId);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  public static void setRecipeStack(ItemStack book, ItemStack recipe) {
    setRecipeResult(book, BuiltInRegistries.ITEM.getKey(recipe.getItem()).toString());
    book.set(
        DataComponents.CONTAINER,
        ItemContainerContents.fromItems(List.of(recipe.copyWithCount(1))));
  }

  public static ItemStack readRecipeStack(ItemStack book) {
    List<ItemStack> stored = new ArrayList<>();
    book.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
        .nonEmptyItems()
        .forEach(stack -> stored.add(stack.copyWithCount(1)));
    if (!stored.isEmpty()) return stored.get(0);
    ResourceLocation id = ResourceLocation.tryParse(readRecipeResult(book));
    return id == null
        ? ItemStack.EMPTY
        : BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
  }

  @Override
  public Component getName(ItemStack stack) {
    ItemStack result = resultStack(stack);
    return result.isEmpty()
        ? super.getName(stack)
        : Component.translatable(
            "item.kaleidoscope_grilling.skewer_recipe_book.recorded", recipeDisplayName(result));
  }

  private static Component recipeDisplayName(ItemStack result) {
    ResourceLocation id = BuiltInRegistries.ITEM.getKey(result.getItem());
    if (id.getNamespace().equals(KaleidoscopeGrilling.MOD_ID)
        && id.getPath().startsWith("raw_")
        && id.getPath().endsWith("_skewer")) {
      String type = id.getPath().substring(4, id.getPath().length() - 7);
      return Component.translatable("item.kaleidoscope_grilling.skewer_recipe_name." + type);
    }
    if (result.is(ModItems.SECRET_SKEWER.get())) {
      String creator = SecretSkewerItem.getCreator(result);
      return creator.isEmpty()
          ? result.getHoverName()
          : Component.translatable("item.kaleidoscope_grilling.skewer_recipe_name.custom", creator);
    }
    return result.getHoverName();
  }

  @Override
  public void appendHoverText(
      ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    String resultId = readRecipeResult(stack);
    if (!resultId.isEmpty()) {
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.usage")
              .withStyle(ChatFormatting.GRAY));
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.wall_usage")
              .withStyle(ChatFormatting.DARK_GRAY));
    } else {
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.recipe_book_empty")
              .withStyle(ChatFormatting.DARK_GRAY));
      tooltip.add(
          Component.translatable("tooltip.kaleidoscope_grilling.recipe_book.recording")
              .withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
    String resultId = readRecipeResult(stack);
    ItemStack recorded = readRecipeStack(stack);
    List<ItemStack> custom =
        recorded.is(ModItems.SECRET_SKEWER.get())
            ? SkeweringHandler.readIngredients(recorded).stream()
                .map(ResourceLocation::tryParse)
                .filter(java.util.Objects::nonNull)
                .map(BuiltInRegistries.ITEM::get)
                .map(ItemStack::new)
                .toList()
            : List.of();
    List<List<String>> recipe = SkewerRecipes.getIngredients(resultId);
    ItemStack output = resultStack(stack);
    if (!custom.isEmpty())
      return Optional.of(
          new RecipeItemTooltip(
              new RecipeItem.RecipeRecord(custom, output, RecipeItem.POT, false), null));
    if (recipe == null || recipe.isEmpty() || output.isEmpty()) return Optional.empty();
    List<ItemStack> inputs = new ArrayList<>();
    for (List<String> selectors : recipe) {
      ItemStack input =
          BuiltInRegistries.ITEM.stream()
              .map(ItemStack::new)
              .filter(
                  candidate ->
                      selectors.stream()
                          .anyMatch(selector -> SkewerRecipes.matchesSelector(candidate, selector)))
              .findFirst()
              .orElse(ItemStack.EMPTY);
      if (input.isEmpty()) return Optional.empty();
      inputs.add(input);
    }
    return Optional.of(
        new RecipeItemTooltip(
            new RecipeItem.RecipeRecord(inputs, output, RecipeItem.POT, false), null));
  }

  private static ItemStack resultStack(ItemStack stack) {
    return readRecipeStack(stack);
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack book = player.getItemInHand(hand);
    ItemStack offhand = player.getOffhandItem();
    if (!offhand.is(Items.STICK)) return InteractionResultHolder.pass(book);

    String resultId = readRecipeResult(book);
    if (resultId.isEmpty()) return InteractionResultHolder.pass(book);

    InteractionResult result = craft(level, player, offhand, book);
    return result == InteractionResult.FAIL
        ? InteractionResultHolder.fail(book)
        : result.consumesAction()
            ? InteractionResultHolder.success(book)
            : InteractionResultHolder.pass(book);
  }

  public static InteractionResult craft(
      Level level, Player player, ItemStack stick, String resultId) {
    ItemStack book = new ItemStack(ModItems.SKEWER_RECIPE_BOOK.get());
    setRecipeResult(book, resultId);
    return craft(level, player, stick, book);
  }

  public static InteractionResult craft(
      Level level, Player player, ItemStack stick, ItemStack book) {
    if (!stick.is(Items.STICK)) return InteractionResult.PASS;
    String resultId = readRecipeResult(book);
    ItemStack recorded = readRecipeStack(book);
    List<ItemStack> customIngredients =
        recorded.is(ModItems.SECRET_SKEWER.get())
            ? SkeweringHandler.readIngredientStacks(recorded, level.registryAccess())
            : List.of();
    List<List<String>> ingredients =
        !customIngredients.isEmpty()
            ? customIngredients.stream()
                .map(s -> List.of(BuiltInRegistries.ITEM.getKey(s.getItem()).toString()))
                .toList()
            : SkewerRecipes.getIngredients(resultId);
    if (ingredients == null) return InteractionResult.PASS;

    if (level.isClientSide) return InteractionResult.SUCCESS;

    Map<Integer, Integer> consumption = new HashMap<>();
    for (int slot = 0; slot < ingredients.size(); slot++) {
      List<String> acceptable = ingredients.get(slot);
      boolean found = false;
      for (int invSlot = 0; invSlot < player.getInventory().getContainerSize(); invSlot++) {
        if (invSlot == player.getInventory().selected) continue;
        if (invSlot == 40) continue; // offhand slot
        ItemStack invStack = player.getInventory().getItem(invSlot);
        if (invStack.isEmpty()) continue;
        int reserved = consumption.getOrDefault(invSlot, 0);
        if (acceptable.stream()
                .anyMatch(selector -> SkewerRecipes.matchesSelector(invStack, selector))
            && invStack.getCount() > reserved) {
          consumption.put(invSlot, reserved + 1);
          found = true;
          break;
        }
      }
      if (!found) {
        StringBuilder missing = new StringBuilder();
        for (String acc : acceptable) {
          if (missing.length() > 0) missing.append("/");
          missing.append(displaySelector(acc));
        }
        player.displayClientMessage(
            Component.translatable(
                "message.kaleidoscope_grilling.skewer_book_missing", missing.toString(), 1),
            true);
        return InteractionResult.FAIL;
      }
    }

    if (!player.getAbilities().instabuild) {
      consumption.forEach((slot, count) -> player.getInventory().getItem(slot).shrink(count));
      stick.shrink(1);
    }

    ItemStack output;
    if (!customIngredients.isEmpty()) {
      output = recorded.copyWithCount(1);
      SecretSkewerItem.setCooked(output, false);
      SecretSkewerItem.setCreator(output, player);
    } else {
      Item resultItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(resultId));
      output = resultItem == null ? ItemStack.EMPTY : new ItemStack(resultItem);
    }
    if (!output.isEmpty() && !player.getInventory().add(output)) player.drop(output, false);
    level.playSound(
        null,
        player.blockPosition(),
        ModSounds.ACTION_SUCCESS.get(),
        SoundSource.PLAYERS,
        0.7F,
        1.0F);
    return InteractionResult.SUCCESS;
  }

  private static String displaySelector(String selector) {
    if (!selector.startsWith("#")) {
      Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(selector));
      return item == null ? selector : item.getDescription().getString();
    }
    ResourceLocation tagId = ResourceLocation.tryParse(selector.substring(1));
    if (tagId == null) return selector;
    TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
    return BuiltInRegistries.ITEM
        .getTag(tag)
        .flatMap(values -> values.stream().findFirst())
        .map(holder -> holder.value().getDescription().getString())
        .orElse(selector);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Direction face = context.getClickedFace();
    if (!face.getAxis().isHorizontal()) return InteractionResult.PASS;
    ItemStack book = context.getItemInHand();
    Level level = context.getLevel();
    if (!level.isClientSide && readRecipeStack(book).isEmpty()) return InteractionResult.FAIL;
    BlockPos target = context.getClickedPos().relative(face);
    if (!level.getBlockState(target).canBeReplaced()) return InteractionResult.FAIL;
    BlockState state =
        ModBlocks.SKEWER_RECIPE.get().defaultBlockState().setValue(SkewerRecipeBlock.FACING, face);
    if (!state.canSurvive(level, target)) return InteractionResult.FAIL;
    if (!level.isClientSide) {
      level.setBlock(target, state, 3);
      if (level.getBlockEntity(target) instanceof SkewerRecipeBlockEntity recipe)
        recipe.setRecipeBook(book);
      level.playSound(null, target, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
      if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild)
        book.shrink(1);
    }
    return InteractionResult.sidedSuccess(level.isClientSide);
  }
}
